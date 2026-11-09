import { beforeEach, describe, expect, it, vi } from 'vitest';
import { toast } from 'sonner';
import { notifyLowStock } from '@/lib/notifications';

// Se reemplaza sonner por un doble de prueba: no hay pantalla, solo importa CÓMO se lo llama.
vi.mock('sonner', () => ({ toast: { warning: vi.fn(), error: vi.fn() } }));

describe('notifyLowStock', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('stays silent when nothing needs restocking', () => {
    notifyLowStock(0);
    expect(toast.warning).not.toHaveBeenCalled();
  });

  it('uses the singular for one product', () => {
    notifyLowStock(1);
    expect(toast.warning).toHaveBeenCalledWith(
      'Atención: Stock Bajo',
      expect.objectContaining({ description: 'Hay 1 producto que necesita reposición.' }),
    );
  });

  it('uses the plural for several products', () => {
    notifyLowStock(3);
    expect(toast.warning).toHaveBeenCalledWith(
      'Atención: Stock Bajo',
      expect.objectContaining({ description: 'Hay 3 productos que necesitan reposición.' }),
    );
  });

  it('is a warning, never an error, and always reuses the same toast', () => {
    notifyLowStock(2);
    notifyLowStock(2);
    expect(toast.error).not.toHaveBeenCalled();
    const ids = vi.mocked(toast.warning).mock.calls.map(([, options]) => options?.id);
    expect(new Set(ids).size).toBe(1);
  });
});
