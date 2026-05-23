# Frontend Project Guide

## Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| Framework | Vue 3 | 3.5.x |
| Build Tool | Vite | 8.x |
| Language | TypeScript | 6.x |
| UI Library | Ant Design Vue | 4.x |
| State Management | Pinia | 3.x |
| Router | Vue Router | 5.x |
| HTTP Client | Axios | 1.x |
| API Code Generation | @umijs/openapi | 1.x |
| Package Manager | pnpm | 10.x |

## Project Structure

```
frontend/
├── src/
│   ├── main.ts                    # Entry file
│   ├── App.vue                    # Root component
│   ├── env.d.ts                   # Type declarations
│   ├── api/                       # OpenAPI generated API clients and types
│   │   ├── index.ts
│   │   ├── typings.d.ts
│   │   ├── healthController.ts
│   │   └── userController.ts
│   ├── pages/                     # Page components
│   │   ├── HomePage.vue
│   │   ├── user/
│   │   │   ├── UserLoginPage.vue
│   │   │   └── UserRegisterPage.vue
│   │   └── admin/
│   │       └── UserManagePage.vue
│   ├── components/                # Shared components
│   │   ├── GlobalHeader.vue
│   │   └── GlobalFooter.vue
│   ├── constants/                 # Shared frontend constants
│   │   └── user.ts
│   ├── layouts/                   # Layout components
│   │   └── BasicLayout.vue
│   ├── router/                    # Router config
│   │   └── index.ts
│   ├── stores/                    # Pinia stores
│   │   └── index.ts
│   └── utils/                     # Utilities
│       └── request.ts             # Axios wrapper
├── vite.config.ts                 # Vite config
├── tsconfig.json                  # TypeScript config
├── tsconfig.node.json             # Node environment TS config
├── openapi2ts.config.ts           # OpenAPI code generation config
└── package.json
```

## Code Style

1. **Vue Components**: Use `<script setup lang="ts">` Composition API
2. **Module Import**: Use ES Module (`import/export`)
3. **Path Alias**: Use `@/` to point to `src/` directory
4. **Route Components**: Use lazy loading `() => import(...)`
5. **Language**: Code comments, variable names, and UI text use English
6. **Generated Code**: Keep generated code comments and any manual adjustments in English

## Key Points

1. **API Requests**: Unified in `utils/request.ts` with interceptors configured
2. **CORS Proxy**: Vite configured `/api` proxy to backend `http://localhost:8567`
3. **Code Generation**: Run `pnpm openapi2ts` after starting backend to generate API code
4. **Generated API Clients**: Generated API clients live under `src/api/`; prefer generated controller functions such as `@/api/userController` over handwritten API wrappers
5. **Generated Types**: OpenAPI types are generated in `src/api/typings.d.ts` under the global `API` namespace, such as `API.LoginUserVO` and `API.UserLoginRequest`
6. **User Constants**: User roles and user-related constants should be defined in `src/constants/user.ts`
7. **Login State**: Login state is managed by `useLoginUserStore` in `src/stores/index.ts`
8. **Route Auth**: Protected routes should use route metadata such as `requiresAuth` and `requiresAdmin`, with access enforced by the router guard


## Development Commands

```bash
# Start dev server
pnpm dev

# Build for production
pnpm build

# Type check
pnpm exec vue-tsc --noEmit

# Preview production build
pnpm preview

# Generate API code from OpenAPI (requires backend running)
pnpm openapi2ts
```
