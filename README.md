# 🏋️‍♂️ Fitness & Nutrition E-Commerce Platform

Hệ thống website thương mại điện tử chuyên cung cấp thực phẩm bổ sung thể hình (Whey Protein, Mass Gainer, Pre-workout, Vitamin...). Đây là đồ án môn học Phát triển ứng dụng với J2EE.

## 🚀 Công nghệ sử dụng (Tech Stack)
* **Backend:** Java, Spring Boot (dựa trên chuẩn J2EE)
* **Database & ORM:** MySQL, Spring Data JPA / Hibernate
* **Security:** Spring Security & JWT (hoặc Session)
* **Frontend:** Thymeleaf / HTML / CSS / Bootstrap (Hoặc cập nhật nếu bạn dùng React/Vue)
* **Công cụ khác:** Maven, Lombok, GitFlow

## 🛒 Các tính năng chính (Key Features)
* **Xác thực người dùng:** Đăng nhập, đăng ký, phân quyền (Admin / Customer).
* **Quản lý sản phẩm:** Hiển thị danh mục thực phẩm bổ sung, lọc tìm kiếm theo giá/loại.
* **Giỏ hàng (Shopping Cart):** Thêm/sửa/xóa sản phẩm trong giỏ hàng.
* **Thanh toán (Checkout):** Xử lý đặt hàng và trừ số lượng tồn kho an toàn với Transaction Management.
* **Quản trị viên (Admin Panel):** Thêm/sửa/xóa sản phẩm (CRUD), quản lý đơn hàng.

///////

---

## ⚡ Chạy nhanh (Quick Start) — dùng MySQL XAMPP

Ứng dụng mặc định chạy **cổng 8082** (một origin duy nhất: chat AI dùng session giỏ hàng — đừng trộn với cổng khác). Có thể dùng MySQL XAMPP hoặc MySQL từ `docker compose up` (chỉ `mysql`) tùy cấu hình.

### Bước 1: Bật MySQL và tạo database

1. Mở **XAMPP Control Panel** → Start **MySQL**.
2. Vào **phpMyAdmin** (http://localhost/phpmyadmin) hoặc MySQL command line, chạy:

```sql
CREATE DATABASE IF NOT EXISTS fitness_nutrition_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'fitness_user'@'localhost' IDENTIFIED BY 'fitness_pass';
GRANT ALL PRIVILEGES ON fitness_nutrition_db.* TO 'fitness_user'@'localhost';
FLUSH PRIVILEGES;
```

### Bước 2: Chạy ứng dụng

```powershell
.\mvnw.cmd spring-boot:run
```

Đợi log xuất hiện `Started J2eeFitnessNutritionEcommerceApplication` và `Tomcat started on port(s): 8082`.

### Bước 3: Mở web và đăng nhập

| Mục | Giá trị |
|-----|--------|
| **Trang chủ** | http://localhost:8082 |
| **Admin** | http://localhost:8082/login → `admin@fitshop.com` / `admin123` |
| **User** | `user@fitshop.com` / `user123` |

Lần đầu chạy, app sẽ tự tạo bảng và dữ liệu mẫu (admin, user, danh mục, sản phẩm, coupon).

**Chạy cả app trong Docker** (`docker compose --profile docker-app up --build`): web cũng ở **http://localhost:8082** (đã map `8082:8080`), trùng với chạy bằng Maven — cookie/session và chat thêm giỏ hàng không bị lệch cổng.

### Integration test (MySQL thật, không mock)

- **Testcontainers** (tự bật container MySQL 8 khi chạy test — cần **Docker Desktop** chạy):  
  `.\mvnw.cmd test -Dtest=CheckoutOrderMySqlIntegrationTest`
- **MySQL từ `docker compose`** (chỉ service `mysql` trên `localhost:3306`): bật biến rồi chạy:  
  `$env:INTEGRATION_TEST_USE_HOST_MYSQL='true'; .\mvnw.cmd test -Dtest=CheckoutOrderComposeMysqlIntegrationTest`  
  (mặc định user/pass giống `compose.yaml`: `fitness_user` / `fitness_pass`.)

> **Lỗi "Communications link failure"?** → MySQL chưa chạy hoặc chưa tạo DB/user. Làm lại Bước 1.
