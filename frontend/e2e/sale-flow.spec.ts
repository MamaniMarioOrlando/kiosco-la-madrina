import { expect, test, type APIRequestContext } from '@playwright/test';
import { API_URL, E2E_ADMIN } from './env';

/**
 * Prepara los datos por la API, no por la pantalla: el test es sobre VENDER, no sobre dar de alta productos.
 * Cada corrida usa nombres y códigos únicos, así puede repetirse sobre la misma base sin chocar.
 */
async function seedProduct(request: APIRequestContext, price: number) {
  const signin = await request.post(`${API_URL}/auth/signin`, { data: E2E_ADMIN });
  expect(signin.ok(), 'el admin de e2e puede loguearse por la API').toBeTruthy();
  const { token } = await signin.json();
  const headers = { Authorization: `Bearer ${token}` };

  const run = Date.now().toString(36);
  const category = await request.post(`${API_URL}/categories`, { headers, data: { name: `E2E ${run}` } });
  expect(category.ok()).toBeTruthy();

  const product = await request.post(`${API_URL}/products`, {
    headers,
    data: {
      name: `Alfajor E2E ${run}`,
      barcode: `e2e-${run}`,
      price,
      stockQuantity: 10,
      categoryId: (await category.json()).id,
    },
  });
  expect(product.ok()).toBeTruthy();
  return (await product.json()) as { name: string };
}

/** 1234.56 → "1.234,56". Se busca el número sin el "$ ", porque Intl lo separa con un espacio especial. */
const pesos = (amount: number) => amount.toLocaleString('es-AR', { minimumFractionDigits: 2 });

test('a cashier logs in, sells a product and finds the sale in the history', async ({ page, request }) => {
  const product = await seedProduct(request, 1234.56);

  // 1. Login, como lo haría una persona: por las etiquetas visibles, no por clases CSS.
  await page.goto('/login');
  await page.getByLabel('Usuario').fill(E2E_ADMIN.username);
  await page.getByLabel('Contraseña').fill(E2E_ADMIN.password);
  await page.getByRole('button', { name: 'Ingresar' }).click();
  await expect(page).toHaveURL('/');

  // 2. Venta: busca el producto, lo agrega al carrito y paga con $2.000 en efectivo.
  await page.getByRole('link', { name: 'Nueva Venta' }).click();
  await page.getByPlaceholder('Nombre o código de barras...').fill(product.name);
  await page.getByText(product.name).click();

  await expect(page.getByText('1 items')).toBeVisible();
  await page.getByPlaceholder('0.00').fill('2000');
  await expect(page.getByText(pesos(765.44))).toBeVisible(); // vuelto calculado en centavos

  await page.getByRole('button', { name: 'Efectivo' }).click();
  await expect(page.getByText('Venta registrada con éxito')).toBeVisible();

  // 3. Historial: la venta más reciente es la nuestra, con el producto y el total correctos.
  await page.getByRole('link', { name: 'Historial' }).click();
  const latestSale = page.getByRole('row').nth(1);
  await expect(latestSale).toContainText(E2E_ADMIN.username);
  await expect(latestSale).toContainText('EFECTIVO');
  await expect(latestSale).toContainText(pesos(1234.56));

  await latestSale.getByRole('button', { name: 'Detalles' }).click();
  await expect(page.getByRole('cell', { name: product.name })).toBeVisible();
});
