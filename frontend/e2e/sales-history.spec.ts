import { expect, test, type APIRequestContext } from '@playwright/test';
import { API_URL, E2E_ADMIN } from './env';

/** 9 ventas de hoy por la API: con 8 por página, alcanza para que el historial tenga una segunda página. */
async function seedNineSalesToday(request: APIRequestContext) {
  const signin = await request.post(`${API_URL}/auth/signin`, { data: E2E_ADMIN });
  expect(signin.ok()).toBeTruthy();
  const headers = { Authorization: `Bearer ${(await signin.json()).token}` };

  const run = Date.now().toString(36);
  const category = await request.post(`${API_URL}/categories`, { headers, data: { name: `Historial ${run}` } });
  const product = await request.post(`${API_URL}/products`, {
    headers,
    data: { name: `Chicle ${run}`, barcode: `hist-${run}`, price: 10, stockQuantity: 50, categoryId: (await category.json()).id },
  });
  const productId = (await product.json()).id;

  for (let i = 0; i < 9; i++) {
    const sale = await request.post(`${API_URL}/sales`, {
      headers,
      data: { items: [{ productId, quantity: 1 }], paymentMethod: 'CASH' },
    });
    expect(sale.ok()).toBeTruthy();
  }
}

test('the sales history asks the API once per page change and once per view change', async ({ page, request }) => {
  await seedNineSalesToday(request);

  // Cada pedido de una página del historial que hace el navegador (pasa por el proxy /api).
  const isHistoryPage = (url: string) => new URL(url).pathname === '/api/sales';
  const historyRequests: string[] = [];
  page.on('request', (req) => {
    if (isHistoryPage(req.url())) historyRequests.push(req.url());
  });
  // Espera la RESPUESTA, no el texto: "Página 2" se dibuja con lo pedido, antes de que salga el pedido.
  // Un pedido duplicado sale en el mismo render que el primero, así que ya está contado cuando este responde.
  const requestsDuring = async (action: () => Promise<void>) => {
    const before = historyRequests.length;
    const response = page.waitForResponse((res) => isHistoryPage(res.url()));
    await action();
    await response;
    return historyRequests.slice(before);
  };

  await page.goto('/login');
  await page.getByLabel('Usuario').fill(E2E_ADMIN.username);
  await page.getByLabel('Contraseña').fill(E2E_ADMIN.password);
  await page.getByRole('button', { name: 'Ingresar' }).click();
  await expect(page).toHaveURL('/');

  const firstLoad = await requestsDuring(async () => {
    await page.goto('/sales/history');
    await expect(page.getByText(/Página 1 de \d+/)).toBeVisible();
  });
  expect(firstLoad).toHaveLength(1);

  const nextPage = await requestsDuring(async () => {
    await page.getByRole('button', { name: 'Siguiente' }).click();
    await expect(page.getByText(/Página 2 de \d+/)).toBeVisible();
  });
  expect(nextPage).toHaveLength(1);
  expect(nextPage[0]).toContain('page=1');

  // Antes, el botón pedía la página y además un useEffect sobre la vista la volvía a pedir: 2 pedidos.
  const switchView = await requestsDuring(async () => {
    await page.getByRole('button', { name: 'Histórico Completo' }).click();
    await expect(page.getByText('Total Histórico')).toBeVisible();
  });
  expect(switchView).toHaveLength(1);
  expect(switchView[0]).not.toContain('date='); // sin fecha = todo el historial
  expect(switchView[0]).toContain('page=0'); // cambiar de vista vuelve a la primera página
});
