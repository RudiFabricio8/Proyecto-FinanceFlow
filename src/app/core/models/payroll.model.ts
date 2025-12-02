export interface PayrollModel {
  id?: string;
  period?: string;
  total?: number;
  items?: Array<Record<string, any>>;
}
