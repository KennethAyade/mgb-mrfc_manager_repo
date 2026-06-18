import { UserMrfcAccess } from '../../models';
import { canUploadToMrfc } from '../../services/documentUploadAccess.service';

describe('document upload MRFC access', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('allows administrators without an assignment lookup', async () => {
    const lookup = jest.spyOn(UserMrfcAccess, 'findOne');

    await expect(
      canUploadToMrfc({ userId: 1, role: 'ADMIN' }, 9)
    ).resolves.toBe(true);
    expect(lookup).not.toHaveBeenCalled();
  });

  it('allows a regular user with active MRFC access', async () => {
    jest.spyOn(UserMrfcAccess, 'findOne').mockResolvedValue({ id: 1 } as any);

    await expect(
      canUploadToMrfc({ userId: 18, role: 'USER' }, 9)
    ).resolves.toBe(true);
  });

  it('denies a regular user without active MRFC access', async () => {
    jest.spyOn(UserMrfcAccess, 'findOne').mockResolvedValue(null);

    await expect(
      canUploadToMrfc({ userId: 18, role: 'USER' }, 8)
    ).resolves.toBe(false);
  });
});

