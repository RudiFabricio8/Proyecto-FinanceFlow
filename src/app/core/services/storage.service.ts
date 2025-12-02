export class StorageService {
  static set(key: string, value: any) { try { localStorage.setItem(key, JSON.stringify(value)); } catch(e){} }
  static get<T = any>(key: string): T | null { try { const v = localStorage.getItem(key); return v ? JSON.parse(v) as T : null; } catch(e){ return null; } }
  static remove(key: string) { try { localStorage.removeItem(key); } catch(e){} }
}
