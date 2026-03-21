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

Ứng dụng đã cấu hình **không** dùng Docker Compose khi chạy local; dùng MySQL trên máy (XAMPP) để khởi động nhanh.

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

Đợi log xuất hiện `Started J2eeFitnessNutritionEcommerceApplication` và `Tomcat started on port(s): 8080`.

### Bước 3: Mở web và đăng nhập

| Mục | Giá trị |
|-----|--------|
| **Trang chủ** | http://localhost:8080 |
| **Admin** | http://localhost:8080/login → `admin@fitshop.com` / `admin123` |
| **User** | `user@fitshop.com` / `user123` |

Lần đầu chạy, app sẽ tự tạo bảng và dữ liệu mẫu (admin, user, danh mục, sản phẩm, coupon).

> **Lỗi "Communications link failure"?** → MySQL chưa chạy hoặc chưa tạo DB/user. Làm lại Bước 1.
