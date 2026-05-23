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

## Key Points

1. **API Requests**: Unified in `utils/request.ts` with interceptors configured
2. **Environment Variables**: Sensitive info uses env vars, e.g., `${MYSQL_PASSWORD:}`
3. **CORS Proxy**: Vite configured `/api` proxy to backend `http://localhost:8567`
4. **Type Safety**: Vue components use `.vue` suffix, TypeScript auto-recognizes
5. **Code Generation**: Run `pnpm openapi2ts` after starting backend to generate API code


## Development Commands

```bash
# Start dev server
pnpm dev

# Build for production
pnpm build

# Preview production build
pnpm preview

# Generate API code from OpenAPI (requires backend running)
pnpm openapi2ts
```
