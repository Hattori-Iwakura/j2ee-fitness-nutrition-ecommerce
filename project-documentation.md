# FitShop - Fitness & Nutrition E-Commerce

> University Final Project — J2EE Application Development Course

---

## 1. Project Overview

FitShop is a full-featured e-commerce web application for fitness and nutrition supplements (Whey Protein, Mass Gainer, Pre-workout, Vitamins). Built as a monolithic MVC application with server-side rendering using Spring Boot and Thymeleaf.

### Key Business Features
- Product catalog with categories, search, and pagination
- Product variants (Flavor, Weight) with independent pricing and stock
- Session-based shopping cart
- Checkout with order creation and automatic stock deduction
- Order tracking with status workflow (Pending → Confirmed → Shipping → Delivered / Cancelled)
- Admin panel for full CRUD management

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.3 |
| Web/MVC | Spring MVC + Thymeleaf | - |
| Security | Spring Security (Session-based) | 7.0.3 |
| ORM | Spring Data JPA (Hibernate) | 7.2.4 |
| Database | MySQL | 8.0 |
| Validation | Jakarta Bean Validation | - |
| CSS | Bootstrap 5 + Bootstrap Icons | 5.3.3 |
| Build Tool | Maven | - |
| Infrastructure | Docker & Docker Compose | - |
| Utility | Lombok | - |

### Architecture Rules
- **Monolithic MVC** — all pages server-side rendered via Thymeleaf
- **`@Controller`** only — no `@RestController`, no JSON APIs unless explicitly required
- **Session-based auth** — no JWT tokens
- **CSRF enabled** — Thymeleaf `th:action` handles tokens automatically

---

## 3. Project Structure

```
j2ee-fitness-nutrition-ecommerce/
├── docs/                          # Internal documentation (gitignored)
├── src/main/java/com/example/j2ee_fitness_nutrition_ecommerce/
│   ├── config/
│   │   ├── SecurityConfig.java        # Spring Security filter chain, BCrypt, role-based access
│   │   └── DataInitializer.java       # Seeds admin/user accounts + sample products on first run
│   ├── controller/
│   │   ├── HomeController.java        # GET / — home page with categories
│   │   ├── AuthController.java        # GET/POST /login, /register
│   │   ├── ProductController.java     # GET /products, /products/{slug}
│   │   ├── CartController.java        # GET/POST /cart (add, update, remove, clear)
│   │   ├── CheckoutController.java    # GET/POST /checkout, /checkout/success
│   │   ├── OrderController.java       # GET /orders, /orders/{id}
│   │   └── admin/
│   │       ├── AdminDashboardController.java   # GET /admin
│   │       ├── AdminCategoryController.java    # CRUD /admin/categories
│   │       ├── AdminProductController.java     # CRUD /admin/products + variant management
│   │       ├── AdminOrderController.java       # /admin/orders + status updates
│   │       └── AdminUserController.java        # /admin/users + enable/disable
│   ├── dto/
│   │   ├── RegisterRequest.java       # Registration form with validation
│   │   └── CheckoutRequest.java       # Checkout shipping info form
│   ├── entity/
│   │   ├── User.java                  # id, fullName, email, password, phone, address, role, enabled
│   │   ├── Category.java              # id, name, slug, description, imageUrl, active
│   │   ├── Product.java               # id, name, slug, description, imageUrl, brand, active
│   │   ├── ProductVariant.java        # id, flavor, weight, price, stock, sku, active
│   │   ├── Order.java                 # id, orderCode, status, totalAmount, fullName, phone, address, note
│   │   └── OrderDetail.java           # id, quantity, unitPrice, subtotal
│   ├── enums/
│   │   ├── Role.java                  # USER, ADMIN
│   │   └── OrderStatus.java           # PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
│   ├── repository/                    # Spring Data JPA interfaces (one per entity)
│   ├── service/
│   │   ├── UserService.java           # register, findByEmail, existsByEmail
│   │   ├── CategoryService.java       # CRUD operations
│   │   ├── ProductService.java        # CRUD + search + pagination
│   │   ├── OrderService.java          # createOrder, updateStatus, findByUserEmail
│   │   ├── CartService.java           # Session-based cart operations
│   │   ├── CartItem.java              # Cart item POJO (stored in HttpSession)
│   │   └── impl/                      # Service implementations
│   └── util/                          # Helper classes (reserved)
├── src/main/resources/
│   ├── application.properties         # Main config (datasource, JPA, Thymeleaf)
│   ├── application-docker.properties  # Docker profile (container networking)
│   ├── static/                        # CSS, JS, images (reserved)
│   └── templates/
│       ├── fragments/
│       │   ├── header.html            # <head> + navbar (with sec:authorize)
│       │   ├── footer.html            # Footer + Bootstrap JS
│       │   └── admin-sidebar.html     # Admin sidebar navigation
│       ├── home/index.html            # Landing page with categories
│       ├── auth/login.html            # Login form
│       ├── auth/register.html         # Registration form with validation
│       ├── product/list.html          # Catalog with sidebar, search, pagination
│       ├── product/detail.html        # Product detail with variant selector
│       ├── cart/index.html            # Shopping cart table
│       ├── checkout/index.html        # Checkout form + order summary
│       ├── checkout/success.html      # Order confirmation
│       ├── order/list.html            # User order history
│       ├── order/detail.html          # Order detail with items
│       ├── admin/dashboard/index.html # Admin stats dashboard
│       ├── admin/category/list.html   # Category table
│       ├── admin/category/form.html   # Category create/edit form
│       ├── admin/product/list.html    # Product table
│       ├── admin/product/form.html    # Product create/edit form
│       ├── admin/product/variants.html# Variant management per product
│       ├── admin/order/list.html      # All orders table
│       ├── admin/order/detail.html    # Order detail + status update
│       └── admin/user/list.html       # User table + enable/disable
├── compose.yaml                       # Docker Compose (MySQL + App)
├── Dockerfile                         # Multi-stage build (Maven → JRE)
├── pom.xml                            # Maven dependencies
└── CLAUDE.md                          # AI context file for development
```

---

## 4. Database Schema

### Entity Relationship Diagram

```
┌──────────┐       ┌──────────────┐       ┌─────────────────┐
│  users   │       │  categories  │       │    products      │
├──────────┤       ├──────────────┤       ├─────────────────┤
│ id (PK)  │       │ id (PK)      │  1:N  │ id (PK)         │
│ fullName │       │ name         │◄──────│ category_id (FK)│
│ email    │       │ slug         │       │ name             │
│ password │       │ description  │       │ slug             │
│ phone    │       │ imageUrl     │       │ description      │
│ address  │       │ active       │       │ imageUrl         │
│ role     │       └──────────────┘       │ brand            │
│ enabled  │                              │ active           │
│ createdAt│                              │ createdAt        │
└──────────┘                              └─────────────────┘
     │                                           │
     │ 1:N                                       │ 1:N
     ▼                                           ▼
┌──────────────┐                        ┌──────────────────┐
│   orders     │                        │ product_variants  │
├──────────────┤                        ├──────────────────┤
│ id (PK)      │                        │ id (PK)          │
│ user_id (FK) │                        │ product_id (FK)  │
│ orderCode    │                        │ flavor           │
│ status       │                        │ weight           │
│ totalAmount  │                        │ price            │
│ fullName     │                        │ stock            │
│ phone        │                        │ sku              │
│ address      │                        │ active           │
│ note         │                        └──────────────────┘
│ createdAt    │                                 ▲
└──────────────┘                                 │
     │                                           │
     │ 1:N                                       │ N:1
     ▼                                           │
┌──────────────────┐                             │
│  order_details   │                             │
├──────────────────┤                             │
│ id (PK)          │                             │
│ order_id (FK)    │                             │
│ variant_id (FK)  │─────────────────────────────┘
│ quantity         │
│ unitPrice        │
│ subtotal         │
└──────────────────┘
```

### Enum Values

| Enum | Values |
|---|---|
| `Role` | `USER`, `ADMIN` |
| `OrderStatus` | `PENDING` → `CONFIRMED` → `SHIPPING` → `DELIVERED` / `CANCELLED` |

---

## 5. Security Configuration

### URL Access Control

| URL Pattern | Access |
|---|---|
| `/`, `/products/**`, `/categories/**`, `/search/**` | Public |
| `/register`, `/login`, `/css/**`, `/js/**`, `/images/**` | Public |
| `/cart/**`, `/checkout/**`, `/orders/**` | Authenticated (USER or ADMIN) |
| `/admin/**` | ADMIN only |

### Authentication Flow
1. User submits login form → Spring Security processes via `CustomUserDetailsService`
2. Loads user by email from database
3. Verifies BCrypt-encoded password
4. Creates session with `ROLE_USER` or `ROLE_ADMIN` authority
5. Redirects: ADMIN → `/admin`, USER → `/`

### CSRF Protection
- Enabled by default (Spring Security 7)
- Thymeleaf `th:action` automatically injects CSRF token in all forms
- All state-changing operations use `POST` method

---

## 6. Core Feature Details

### 6.1 Product Catalog
- **Browse all**: `GET /products` — paginated (9 per page), sorted by newest
- **Filter by category**: `GET /products?category={slug}` — sidebar category links
- **Search**: `GET /products?keyword={term}` — case-insensitive name search
- **Product detail**: `GET /products/{slug}` — shows all variants with flavor/weight/price/stock

### 6.2 Shopping Cart (Session-based)
- Cart stored as `List<CartItem>` in `HttpSession`
- **Add**: `POST /cart/add` — adds variant to cart (increments quantity if already exists)
- **Update**: `POST /cart/update` — change quantity (removes if ≤ 0)
- **Remove**: `POST /cart/remove` — remove specific variant
- **Clear**: `POST /cart/clear` — empty entire cart
- Cart persists across page navigations within the same session

### 6.3 Checkout Process
1. User clicks "Proceed to Checkout" from cart
2. Shipping form pre-fills from user profile (name, phone, address)
3. Order summary displays all cart items and total
4. On submit:
   - Validates shipping info
   - Checks stock availability for all variants
   - Deducts stock from `product_variants`
   - Creates `Order` + `OrderDetail` records
   - Generates unique order code (`FS-XXXXXXXX`)
   - Clears cart
   - Redirects to success page

### 6.4 Order History
- **List**: `GET /orders` — user sees their orders sorted by newest
- **Detail**: `GET /orders/{id}` — full order with items, prices, shipping info, status

### 6.5 Admin Panel
- **Dashboard**: `GET /admin` — total orders, revenue, products, users
- **Categories**: Full CRUD at `/admin/categories`
- **Products**: Full CRUD at `/admin/products` with inline variant management at `/admin/products/{id}/variants`
- **Orders**: List + detail + status updates at `/admin/orders`
- **Users**: List + enable/disable toggle at `/admin/users`

---

## 7. Infrastructure

### Docker Compose Setup

```yaml
services:
  mysql:       # MySQL 8.0, port 3306, with volume persistence and healthcheck
  app:         # Spring Boot app, port 8080, depends on healthy MySQL
```

### Running Locally (Development)

```bash
# 1. Start MySQL
docker compose up -d mysql

# 2. Start Spring Boot
./mvnw spring-boot:run

# 3. Open http://localhost:8080
```

### Running with Docker (Full Stack)

```bash
docker compose up --build
```

### Dockerfile
- **Stage 1 (build)**: `maven:3.9-eclipse-temurin-21` — compiles and packages the JAR
- **Stage 2 (run)**: `eclipse-temurin:21-jre` — lightweight runtime image

---

## 8. Test Accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@fitshop.com` | `admin123` |
| User | `user@fitshop.com` | `user123` |

These are auto-created by `DataInitializer.java` on first startup when the database is empty.

### Sample Data (seeded automatically)
- **3 categories**: Whey Protein, Mass Gainer, Pre-Workout
- **3 products** with multiple variants:
  - Optimum Nutrition Gold Standard Whey (Chocolate 2lbs, Vanilla 2lbs, Chocolate 5lbs)
  - Serious Mass (Chocolate 6lbs, Banana 6lbs)
  - C4 Original Pre-Workout (Fruit Punch 30sv, Blue Raspberry 30sv)

---

## 9. Development Roadmap

| Phase | Description | Status |
|---|---|---|
| Phase 1 | Project Setup & Docker Configuration | Done |
| Phase 2 | Database Design & Entity Mapping (6 entities) | Done |
| Phase 3 | Spring Security (Login/Register, ADMIN/USER roles) | Done |
| Phase 4 | Core Features (Catalog, Cart, Checkout, Orders) | Done |
| Phase 5 | Admin Panel (CRUD for all entities) | Done |
| Phase 6 | UI Integration with Thymeleaf & Bootstrap 5 | Done |

### Potential Enhancements
- [ ] Custom error pages (404, 403, 500)
- [ ] Product image upload (file storage)
- [ ] Payment gateway integration
- [ ] Email notifications (order confirmation)
- [ ] Product reviews and ratings
- [ ] Coupon / discount code system
- [ ] Wishlist functionality
- [ ] Export orders to CSV/PDF
- [ ] Responsive design polish for mobile

---

## 10. Key Design Decisions

| Decision | Rationale |
|---|---|
| Session-based cart (not DB) | Simpler for course scope; no guest-to-user cart merge needed |
| `ddl-auto=update` | Auto schema generation for development; would use Flyway in production |
| Prices in VND (BigDecimal) | Vietnamese market context; BigDecimal avoids floating-point rounding issues |
| Slug-based URLs for products | SEO-friendly URLs (`/products/on-gold-standard-whey` vs `/products/1`) |
| Order snapshots shipping info | Order stores its own fullName/phone/address, not a FK to user — preserves data at time of order |
| DataInitializer for seed data | Automatic demo data on first run; skips if data already exists |
