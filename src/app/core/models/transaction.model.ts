export interface Transaction {
  date: string;
  description: string;
  category: string;
  amount: number;
  isPositive: boolean;
}