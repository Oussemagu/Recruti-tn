export interface User {
  id: string;
  email: string;
  fullName: string;
  phone?: string;
  avatar?: string;
  createdAt?: Date;
  updatedAt?: Date;
}
