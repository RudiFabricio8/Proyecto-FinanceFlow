export class ValidationService {
  static required(value: any): boolean { return value !== null && value !== undefined && value !== ''; }
  static isNumber(value: any): boolean { return !isNaN(Number(value)); }
}
