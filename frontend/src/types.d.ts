/// <reference types="vite/client" />

declare module 'react' {
  export function useState<T>(initialState?: T | (() => T)): [T, (newState: any) => void];
  export function useEffect(effect: any, deps?: any[]): void;
  export function useCallback<T extends (...args: any[]) => any>(callback: T, deps: any[]): T;
  export function useMemo<T>(factory: () => T, deps: any[]): T;
  export function useRef<T>(initialValue?: T): { current: T };
  export function createContext<T>(defaultValue: T): any;
  export function useContext<T>(context: any): T;
  const React: any;
  export default React;
}

declare module 'react/jsx-runtime' {
  export const jsx: any;
  export const jsxs: any;
  export const Fragment: any;
}

declare module 'react-dom/client' {
  export const createRoot: any;
}

declare module 'lucide-react' {
  export const Link2: any;
  export const BarChart3: any;
  export const Zap: any;
  export const ShieldCheck: any;
  export const Activity: any;
  export const Copy: any;
  export const Check: any;
  export const ExternalLink: any;
  export const ArrowRight: any;
}

declare module 'vite' {
  export const defineConfig: any;
}

declare module '@vitejs/plugin-react' {
  const react: any;
  export default react;
}

declare namespace JSX {
  interface IntrinsicElements {
    [elemName: string]: any;
  }
}
