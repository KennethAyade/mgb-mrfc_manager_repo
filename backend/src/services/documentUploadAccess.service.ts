import { UserMrfcAccess } from '../models';

export interface UploadPrincipal {
  userId: number;
  role: 'SUPER_ADMIN' | 'ADMIN' | 'USER';
}

export const canUploadToMrfc = async (
  user: UploadPrincipal,
  mrfcId: number
): Promise<boolean> => {
  if (user.role === 'ADMIN' || user.role === 'SUPER_ADMIN') {
    return true;
  }

  const access = await UserMrfcAccess.findOne({
    where: {
      user_id: user.userId,
      mrfc_id: mrfcId,
      is_active: true
    }
  });

  return access !== null;
};

