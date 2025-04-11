# Tài liệu API của SamNgocLinhPJ

## Mục lục
- [Giới thiệu](#giới-thiệu)
- [Các cải tiến mới](#các-cải-tiến-mới)
- [Health Status](#health-status)
- [Cấu trúc Response Chuẩn](#cấu-trúc-response-chuẩn)
- [Xác thực (`/api/auth`)](#xác-thực-apiauth)
- [Người dùng (`/api/users`, `/api/account`)](#người-dùng-apiusers-apiaccount)
- [Sản phẩm (`/api/products`)](#sản-phẩm-apiproducts)
- [Danh mục (`/api/categories`)](#danh-mục-apicategories)
- [Đơn hàng (`/api/orders`)](#đơn-hàng-apiorders)
- [Tin tức (`/api/news`)](#tin-tức-apinews)
- [Liên hệ (`/api/contact`)](#liên-hệ-apicontact)
- [Dịch vụ (`/api/services`)](#dịch-vụ-apiservices)
- [Test (`/api/test`)](#test-apitest)
- [Tài liệu API (Swagger UI)](#tài-liệu-api-swagger-ui)

## Giới thiệu

API này cung cấp các endpoint để tương tác với hệ thống Sâm Ngọc Linh.

**Base URL:** `/` (Ví dụ: `http://yourdomain.com/api/auth/login`)

**Xác thực:** API sử dụng chuẩn JWT (JSON Web Token) cho xác thực. Gửi token nhận được sau khi đăng nhập trong header `Authorization` dưới dạng `Bearer {token}`.

**Lưu ý quan trọng về đặt hàng:** Người dùng **bắt buộc phải đăng nhập** để có thể đặt hàng và thanh toán. Hệ thống sẽ từ chối các yêu cầu đặt hàng từ người dùng chưa xác thực và yêu cầu đăng nhập hoặc đăng ký tài khoản.

## Các cải tiến mới

### JWT Authentication
API đã được nâng cấp để sử dụng JWT (JSON Web Token) thay cho token tùy chỉnh trước đây, mang lại nhiều lợi ích:
- Bảo mật cao hơn với signature được mã hóa
- Tích hợp tốt hơn với các chuẩn xác thực hiện đại
- Hỗ trợ refresh token để tránh đăng nhập lại liên tục
- Khả năng lưu trữ thông tin claim trong token

### Rate Limiting
Hệ thống đã tích hợp rate limiting để bảo vệ API khỏi các cuộc tấn công DoS:
- Giới hạn 20 request trong 1 phút cho mỗi IP
- Áp dụng cho tất cả các endpoint, trừ tài nguyên tĩnh và Swagger UI
- Khi vượt quá giới hạn, phản hồi sẽ là 429 Too Many Requests

### Caching
Hệ thống caching hiệu quả được triển khai:
- Cache ngắn hạn (5 phút) cho dữ liệu thay đổi thường xuyên
- Cache trung bình (30 phút) cho danh sách sản phẩm, danh mục
- Cache dài hạn (1 giờ) cho dữ liệu ít thay đổi
- Cache tự động vô hiệu hóa khi dữ liệu được cập nhật

### Bảo mật nâng cao
Cải tiến bảo mật:
- Mã hóa thông tin nhạy cảm trong file cấu hình
- Xử lý CORS một cách an toàn
- Bảo vệ CSRF cho các endpoint quan trọng
- Triển khai Spring Security với cấu hình bảo mật tốt hơn

## Health Status

API cung cấp các endpoint để kiểm tra tình trạng hoạt động (health check) của hệ thống.

### Kiểm tra sức khỏe hệ thống

**Endpoint:** `GET /health` (Endpoint chính)

**Các Endpoint thay thế:**
- `GET /api/health`
- `GET /status`
- `GET /` (Endpoint gốc - cung cấp phản hồi đơn giản hơn)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "API is healthy",
  "data": {
    "status": "UP",
    "timestamp": "2024-03-24T10:15:30",
    "version": "1.0.0",
    "systemInfo": {
      "availableProcessors": 8,
      "freeMemory": "120 MB",
      "totalMemory": "512 MB",
      "maxMemory": "2048 MB"
    }
  }
}
```

**Response cho Endpoint gốc (/):**
```json
{
  "success": true,
  "message": "API is running",
  "data": null
}
```

**Lưu ý:**
- Các endpoint này được thiết kế để tích hợp với hệ thống giám sát frontend (ApiStatus component)
- Endpoint trả về status code 200 để hệ thống được coi là "Online"
- Nếu phản hồi mất hơn 1 giây nhưng dưới 3 giây, API được coi là "Slow"
- Nếu phản hồi mất hơn 3 giây hoặc trả về status khác 200, API được coi là "Offline"

## Cấu trúc Response Chuẩn

**Thành công (HTTP 2xx):**
```json
{
  "success": true,
  "message": "Thông báo thành công (ví dụ: Đăng nhập thành công!)",
  "data": { ... } // Dữ liệu trả về (có thể là object, array, hoặc null)
}
```

**Thất bại (HTTP 4xx, 5xx):**
```json
{
  "success": false,
  "message": "Thông báo lỗi (ví dụ: Email hoặc mật khẩu không đúng)"
  // Không có trường "data"
}
```

## Xác thực (`/api/auth`)

### Gửi OTP xác thực email

**Endpoint:** `POST /api/auth/send-otp`

**Content-Type:** `application/json`
**Request Body:**
```json
{
  "email": "email@example.com"
}
```

**Content-Type:** `text/plain`
**Request Body:**
```
email@example.com
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Mã OTP đã được gửi đến email của bạn!",
  "data": null
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Email không được để trống!"
}
```

**Response (429 Too Many Requests):**
```json
{
  "success": false,
  "message": "Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút."
}
```

**Response (500 Internal Server Error):**
```json
{
  "success": false,
  "message": "An internal server error occurred. Please try again later."
}
```

### Xác thực OTP

**Endpoint:** `POST /api/auth/verify-otp`

**Request Body:**
```json
{
  "email": "email@example.com",
  "otp": "123456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xác thực OTP thành công!",
  "data": null
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Email và mã OTP không được để trống!"
}
```
```json
{
  "success": false,
  "message": "OTP không hợp lệ hoặc đã hết hạn!"
}
```

**Response (429 Too Many Requests):**
```json
{
  "success": false,
  "message": "Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút."
}
```

### Đăng ký tài khoản

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "name": "Nguyễn Văn A",
  "email": "email@example.com",
  "password": "matkhau123",
  "phone": "0123456789"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "email@example.com",
    "phone": "0123456789",
    "role": "USER",
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00"
    // Lưu ý: Password không được trả về
  }
}
```

**Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Email đã tồn tại!"
}
```
```json
{
  "success": false,
  "message": "Email chưa được xác thực!"
}
```
```json
{
  "success": false,
  "message": "Validation failed: name: Tên không được để trống, email: Email không hợp lệ, ..."
}
```

**Response (429 Too Many Requests):**
```json
{
  "success": false,
  "message": "Quá nhiều yêu cầu. Vui lòng thử lại sau."
}
```

**Lưu ý:** Email phải được xác thực OTP thành công trước khi đăng ký.

### Đăng nhập (JWT Authentication)

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "email@example.com",
  "password": "matkhau123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng nhập thành công!",
  "data": {
    "user": {
      "id": 1,
      "name": "Nguyễn Văn A",
      "email": "email@example.com",
      "role": "USER"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Email hoặc mật khẩu không đúng!"
}
```

### Làm mới token (Refresh Token)

**Endpoint:** `POST /api/auth/refresh-token`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token đã được làm mới thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Refresh token không hợp lệ hoặc đã hết hạn!"
}
```

## Người dùng (`/api/users`, `/api/account`)

### Lấy danh sách người dùng

**Endpoint:** `GET /api/users`

**Response (200):**
```json
[
  {
    "id": 1,
    "email": "email@example.com",
    "role": "USER",
    "createdAt": "2023-01-01T12:00:00"
  }
]
```

### Lấy thông tin người dùng theo ID

**Endpoint:** `GET /api/users/{id}`

**Response (200):**
```json
{
  "id": 1,
  "email": "email@example.com",
  "role": "USER",
  "createdAt": "2023-01-01T12:00:00"
}
```

### Cập nhật thông tin người dùng

**Endpoint:** `PUT /api/users/{id}`

**Request Body:**
```json
{
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "address": "123 Đường ABC, Quận XYZ"
}
```

**Response (200):**
```json
{
  "id": 1,
  "email": "email@example.com",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "address": "123 Đường ABC, Quận XYZ",
  "role": "USER",
  "createdAt": "2023-01-01T12:00:00"
}
```

## Sản phẩm (`/api/products`)

### Lấy tất cả sản phẩm

**Endpoint:** `GET /api/products`

**Query Parameters:**
- `page`: Số trang (mặc định: 0)
- `size`: Số lượng sản phẩm mỗi trang (mặc định: 10)
- `sortBy`: Trường sắp xếp (mặc định: "id")
- `direction`: Hướng sắp xếp ("asc" hoặc "desc", mặc định: "desc")
- `status`: Lọc theo trạng thái sản phẩm (ACTIVE, INACTIVE, ALL)

**Lưu ý:**
- Mặc định sẽ trả về **tất cả** sản phẩm khi không truyền tham số status
- Nếu muốn lọc theo trạng thái:
  - `status=ACTIVE` - chỉ sản phẩm đang bán
  - `status=INACTIVE` - chỉ sản phẩm không bán

**Response (200):**
```json
{
  "products": [
    {
      "id": 1,
      "name": "Sâm Ngọc Linh Tươi 5 Năm",
      "slug": "sam-ngoc-linh-tuoi-5-nam",
      "description": "Mô tả chi tiết sản phẩm",
      "price": 1500000.00,
      "originalPrice": 1800000.00,
      "image": "https://example.com/images/sam1.jpg",
      "categoryId": 1,
      "categoryName": "Sâm tươi",
      "stock": 20,
      "sold": 5,
      "rating": 4.8,
      "status": "ACTIVE",
      "createdAt": "2023-01-01T12:00:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 5,
  "totalPages": 1
}
```

### Lấy sản phẩm theo ID

**Endpoint:** `GET /api/products/{id}`

**Response (200):**
```json
{
  "id": 1,
  "name": "Sâm Ngọc Linh Tươi 5 Năm",
  "slug": "sam-ngoc-linh-tuoi-5-nam",
  "description": "Mô tả chi tiết sản phẩm",
  "price": 1500000.00,
  "originalPrice": 1800000.00,
  "image": "https://example.com/images/sam1.jpg",
  "categoryId": 1,
  "categoryName": "Sâm tươi",
  "stock": 20,
  "sold": 5,
  "rating": 4.8,
  "status": "ACTIVE",
  "createdAt": "2023-01-01T12:00:00"
}
```

### Lấy sản phẩm theo Slug

**Endpoint:** `GET /api/products/slug/{slug}`

**Response (200):** *Giống như response của API lấy sản phẩm theo ID*

### Thêm sản phẩm mới (chỉ dành cho admin)

**Endpoint:** `POST /api/products`

**Headers:**
- `Authorization`: `Bearer {token}`

**Request Body:**
```json
{
  "name": "Sâm Ngọc Linh Tươi 5 Năm",
  "slug": "sam-ngoc-linh-tuoi-5-nam",
  "description": "Mô tả chi tiết sản phẩm",
  "price": 1500000.00,
  "originalPrice": 1800000.00,
  "image": "https://example.com/images/sam1.jpg",
  "categoryId": 1,
  "stock": 20,
  "status": "ACTIVE"
}
```

**Lưu ý về trạng thái sản phẩm (status):**
- `ACTIVE`: Sản phẩm đang được bán (mặc định)
- `INACTIVE`: Sản phẩm tạm ngừng bán
- `OUT_OF_STOCK`: Sản phẩm đã hết hàng

**Response (201):** *Thông tin sản phẩm đã tạo, giống như response của API lấy sản phẩm theo ID*

### Cập nhật sản phẩm (chỉ dành cho admin)

**Endpoint:** `PUT /api/products/{id}`

**Headers:**
- `Authorization`: `Bearer {token}`
- `Content-Type`: `application/json`

**Path Variable:**
- `{id}`: ID của sản phẩm cần cập nhật (số nguyên)

**Request Body:**
```json
{
  "name": "Sâm Ngọc Linh Tươi 5 Năm",
  "slug": "sam-ngoc-linh-tuoi-5-nam",
  "description": "Mô tả chi tiết sản phẩm đã cập nhật",
  "price": 1500000.00,
  "originalPrice": 1800000.00,
  "image": "https://example.com/images/sam1.jpg",
  "categoryId": 1,
  "stock": 20,
  "status": "ACTIVE"
}
```

**Trạng thái sản phẩm (status):**
- `ACTIVE`: Sản phẩm đang được bán (mặc định)
- `INACTIVE`: Sản phẩm tạm ngừng bán
- `OUT_OF_STOCK`: Sản phẩm đã hết hàng

**Response (200):** Thông tin sản phẩm đã được cập nhật

**Lưu ý:**
- Chỉ tài khoản có quyền ADMIN mới có thể cập nhật sản phẩm
- Bạn chỉ cần gửi các trường cần cập nhật, không cần gửi tất cả các trường
- Nếu cập nhật tên sản phẩm, hệ thống sẽ kiểm tra xem tên đã tồn tại chưa
- Nếu thay đổi slug, đảm bảo rằng slug là duy nhất
- Đảm bảo categoryId tồn tại trong hệ thống
- Có thể cập nhật trạng thái sản phẩm sang OUT_OF_STOCK khi hết hàng thay vì chỉnh sửa số lượng

**Mã lỗi có thể gặp:**
- 401 Unauthorized: Token không hợp lệ hoặc đã hết hạn
- 403 Forbidden: Không có quyền thực hiện hành động
- 400 Bad Request: Dữ liệu không hợp lệ hoặc ID sản phẩm không phải số nguyên
- 404 Not Found: Không tìm thấy sản phẩm với ID đã chỉ định

### Xóa sản phẩm (chỉ dành cho admin)

**Endpoint:** `DELETE /api/products/{id}`

**Headers:**
- `Authorization`: `Bearer {token}`

**Response (204):** *Không có nội dung*

**Lưu ý:**
- Chỉ tài khoản có quyền ADMIN mới có thể xóa sản phẩm
- Xóa sản phẩm là hành động không thể khôi phục
- ID sản phẩm phải là số nguyên hợp lệ

**Mã lỗi có thể gặp:**
- 401 Unauthorized: Token không hợp lệ hoặc đã hết hạn
- 403 Forbidden: Không có quyền thực hiện hành động
- 400 Bad Request: ID sản phẩm không hợp lệ
- 404 Not Found: Không tìm thấy sản phẩm với ID đã chỉ định

### Tìm kiếm sản phẩm

**Endpoint:** `GET /api/products/search`

**Request Parameters:**
- `keyword`: Từ khóa tìm kiếm
- `page`: Số trang (mặc định: 0)
- `size`: Kích thước trang (mặc định: 10)

**Response (200):** *Tương tự như lấy danh sách sản phẩm*

### Lấy sản phẩm bán chạy nhất

**Endpoint:** `GET /api/products/best-selling`

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Sâm Ngọc Linh 10 năm tuổi",
    "slug": "sam-ngoc-linh-10-nam-tuoi",
    "description": "Sâm Ngọc Linh chất lượng cao từ Kon Tum",
    "price": 1000000,
    "originalPrice": 1200000,
    "image": "/uploads/sam-ngoc-linh-1.jpg",
    "categoryId": 1,
    "categoryName": "Sâm tươi",
    "stock": 10,
    "sold": 5,
    "rating": 4.5,
    "status": "ACTIVE",
    "createdAt": "2023-01-01T12:00:00"
  }
]
```

### Lấy sản phẩm mới nhất

**Endpoint:** `GET /api/products/new-arrivals`

**Response (200):** *Tương tự như lấy sản phẩm bán chạy nhất*

## Danh mục (`/api/categories`)

### Lấy danh sách danh mục

**Endpoint:** `GET /api/categories`

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Sâm tươi",
    "slug": "sam-tuoi",
    "description": "Sâm Ngọc Linh tươi nguyên củ",
    "image": "/uploads/category-sam-tuoi.jpg"
  }
]
```

### Lấy danh mục theo ID

**Endpoint:** `GET /api/categories/{id}`

**Response (200):**
```json
{
  "id": 1,
  "name": "Sâm tươi",
  "slug": "sam-tuoi",
  "description": "Sâm Ngọc Linh tươi nguyên củ",
  "image": "/uploads/category-sam-tuoi.jpg"
}
```

### Lấy sản phẩm theo danh mục

**Endpoint:** `GET /api/categories/{id}/products`

**Request Parameters (tùy chọn):**
- `page`: Số trang (mặc định: 0)
- `size`: Kích thước trang (mặc định: 10)
- `status`: Lọc theo trạng thái sản phẩm (ACTIVE, INACTIVE, ALL)

**Lưu ý:**
- Mặc định sẽ trả về **tất cả** sản phẩm trong danh mục khi không truyền tham số status
- Nếu muốn lọc theo trạng thái:
  - `status=ACTIVE` - chỉ sản phẩm đang bán
  - `status=INACTIVE` - chỉ sản phẩm không bán
  - `status=ALL` - tất cả sản phẩm (giống mặc định)

**Response (200):** *Tương tự như lấy danh sách sản phẩm*

## Đơn hàng (`/api/orders`)

### Lấy tất cả đơn hàng (chỉ dành cho admin)

**Endpoint:** `GET /api/orders`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (200):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "totalAmount": 150000,
    "shippingFee": 30000,
    "status": "PENDING",
    "paymentMethod": "COD",
    "paymentStatus": "PENDING",
    "shippingName": "Nguyễn Văn A",
    "shippingPhone": "0123456789",
    "shippingAddress": "123 Đường ABC, Quận XYZ",
    "shippingEmail": "email@example.com",
    "notes": "Giao hàng giờ hành chính",
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00"
  }
]
```

### Lấy đơn hàng theo ID

**Endpoint:** `GET /api/orders/{id}`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (200):**
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 150000,
  "shippingFee": 30000,
  "status": "PENDING",
  "paymentMethod": "COD",
  "paymentStatus": "PENDING",
  "shippingName": "Nguyễn Văn A",
  "shippingPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, Quận XYZ",
  "shippingEmail": "email@example.com",
  "notes": "Giao hàng giờ hành chính",
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T12:00:00"
}
```

### Lấy đơn hàng của người dùng hiện tại

**Endpoint:** `GET /api/orders/my-orders`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (200):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "totalAmount": 150000,
    "shippingFee": 30000,
    "status": "PENDING",
    "paymentMethod": "COD",
    "paymentStatus": "PENDING",
    "shippingName": "Nguyễn Văn A",
    "shippingPhone": "0123456789",
    "shippingAddress": "123 Đường ABC, Quận XYZ",
    "shippingEmail": "email@example.com",
    "notes": "Giao hàng giờ hành chính",
    "createdAt": "2023-01-01T12:00:00",
    "updatedAt": "2023-01-01T12:00:00"
  }
]
```

### Tạo đơn hàng mới

**Endpoint:** `POST /api/orders`

**Headers:**
- `Authorization`: `Bearer {token_string}` (Bắt buộc)

**Request Body:**
```json
{
  "shippingFee": 30000,
  "paymentMethod": "COD",
  "shippingName": "Nguyễn Văn A",
  "shippingPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, Quận XYZ",
  "shippingEmail": "email@example.com",
  "notes": "Giao hàng giờ hành chính",
  "products": {
    "1": 2,
    "2": 1
  }
}
```

Trong đó `products` là một map với key là product ID và value là số lượng sản phẩm.

**Response (201):**
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 150000,
  "shippingFee": 30000,
  "status": "PENDING",
  "paymentMethod": "COD",
  "paymentStatus": "PENDING",
  "shippingName": "Nguyễn Văn A",
  "shippingPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, Quận XYZ",
  "shippingEmail": "email@example.com",
  "notes": "Giao hàng giờ hành chính",
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T12:00:00"
}
```

**Response (401 Unauthorized):**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Bạn cần đăng nhập để đặt hàng. Vui lòng đăng nhập hoặc tạo tài khoản trước khi thanh toán.",
  "redirectUrl": "/login"
}
```

**Lưu ý quan trọng:**
- Người dùng **bắt buộc phải đăng nhập** để có thể đặt hàng. Hệ thống sẽ từ chối các yêu cầu đặt hàng từ người dùng chưa xác thực.
- Nếu chưa đăng nhập, hệ thống sẽ trả về lỗi 401 với thông báo hướng dẫn đăng nhập.

### Cập nhật trạng thái đơn hàng (chỉ dành cho admin)

**Endpoint:** `PUT /api/orders/{id}/status`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Request Parameters:**
- `status`: Trạng thái đơn hàng (PENDING, PROCESSING, SHIPPING, COMPLETED, CANCELLED)

**Response (200):**
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 150000,
  "shippingFee": 30000,
  "status": "PROCESSING",
  "paymentMethod": "COD",
  "paymentStatus": "PENDING",
  "shippingName": "Nguyễn Văn A",
  "shippingPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, Quận XYZ",
  "shippingEmail": "email@example.com",
  "notes": "Giao hàng giờ hành chính",
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T12:30:00"
}
```

### Cập nhật trạng thái thanh toán (chỉ dành cho admin)

**Endpoint:** `PUT /api/orders/{id}/payment`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Request Parameters:**
- `paymentStatus`: Trạng thái thanh toán (PENDING, COMPLETED, FAILED)

**Response (200):**
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 150000,
  "shippingFee": 30000,
  "status": "PROCESSING",
  "paymentMethod": "COD",
  "paymentStatus": "COMPLETED",
  "shippingName": "Nguyễn Văn A",
  "shippingPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, Quận XYZ",
  "shippingEmail": "email@example.com",
  "notes": "Giao hàng giờ hành chính",
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T12:45:00"
}
```

### Hủy đơn hàng

**Endpoint:** `POST /api/orders/{id}/cancel`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (200):**
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 150000,
  "shippingFee": 30000,
  "status": "CANCELLED",
  "paymentMethod": "COD",
  "paymentStatus": "PENDING",
  "shippingName": "Nguyễn Văn A",
  "shippingPhone": "0123456789",
  "shippingAddress": "123 Đường ABC, Quận XYZ",
  "shippingEmail": "email@example.com",
  "notes": "Giao hàng giờ hành chính",
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T13:00:00"
}
```

### Kiểm tra trạng thái đơn hàng (endpoint công khai)

**Endpoint:** `GET /api/orders/public/{id}`

**Response (200):**
```json
{
  "id": 1,
  "status": "PROCESSING",
  "paymentStatus": "PENDING",
  "createdAt": "2023-01-01T12:00:00"
}
```

## Tin tức (`/api/news`)

### Lấy danh sách tin tức

**Endpoint:** `GET /api/news`

**Request Parameters (tùy chọn):**
- `page`: Số trang (mặc định: 0)
- `size`: Kích thước trang (mặc định: 10)

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Những lợi ích của Sâm Ngọc Linh",
      "slug": "nhung-loi-ich-cua-sam-ngoc-linh",
      "summary": "Tóm tắt về lợi ích của Sâm Ngọc Linh",
      "content": "<p>Nội dung chi tiết về lợi ích của Sâm Ngọc Linh</p>",
      "image": "/uploads/news-sam-ngoc-linh.jpg",
      "category": "news",
      "published": true,
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "unpaged": false,
    "paged": true
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

### Lấy tin tức theo ID

**Endpoint:** `GET /api/news/{id}`

**Response (200):**
```json
{
  "id": 1,
  "title": "Những lợi ích của Sâm Ngọc Linh",
  "slug": "nhung-loi-ich-cua-sam-ngoc-linh",
  "summary": "Tóm tắt về lợi ích của Sâm Ngọc Linh",
  "content": "<p>Nội dung chi tiết về lợi ích của Sâm Ngọc Linh</p>",
  "image": "/uploads/news-sam-ngoc-linh.jpg",
  "category": "news",
  "published": true,
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T12:00:00"
}
```

## Liên hệ (`/api/contact`)

*... (Cần cập nhật) ...*

## Dịch vụ (`/api/services`)

*... (Cần cập nhật) ...*

## Test (`/api/test`)

*... (Cần cập nhật) ...*

## Tài liệu API (Swagger UI)

API Documentation hiện đã có sẵn thông qua Swagger UI.

**URL:** `/swagger-ui/index.html`

Swagger UI cung cấp một giao diện tương tác cho phép:
- Khám phá tất cả các endpoint có sẵn
- Thử nghiệm API trực tiếp từ giao diện
- Xem chi tiết các tham số và phản hồi
- Thực hiện xác thực với JWT token

**Lưu ý bảo mật:** Swagger UI được bảo vệ trong môi trường production. Vui lòng liên hệ với quản trị viên để được cấp quyền truy cập nếu cần thiết. 