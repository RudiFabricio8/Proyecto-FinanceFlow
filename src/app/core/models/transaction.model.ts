export interface Transaction {
  id?: string;
  userId?: string;
  amount: number;
  date: string;
  description: string;
  type: 'INCOME' | 'EXPENSE';
  category: string;
}