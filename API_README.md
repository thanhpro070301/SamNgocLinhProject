# Tài liệu API của SamNgocLinhPJ

## Mục lục
- [Xác thực](#xác-thực)
- [API Người dùng](#api-người-dùng)
- [API Sản phẩm](#api-sản-phẩm)
- [API Danh mục](#api-danh-mục)
- [API Đơn hàng](#api-đơn-hàng)
- [API Tin tức](#api-tin-tức)

## Xác thực

### Gửi OTP xác thực email

**Endpoint:** `POST /api/auth/send-otp`

**Request Body:**
```
email@example.com
```

**Response (200):**
```
Mã OTP đã được gửi đến email của bạn!
```

### Xác thực OTP

**Endpoint:** `POST /api/auth/verify-otp`

**Request Parameters:**
- `email`: Email đăng ký
- `otp`: Mã OTP nhận được

**Response (200):**
```
Xác thực OTP thành công!
```

### Đăng ký tài khoản

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "email": "email@example.com",
  "password": "matkhau123"
}
```

**Response (200):**
```json
{
  "id": 1,
  "email": "email@example.com",
  "role": "USER",
  "createdAt": "2023-01-01T12:00:00"
}
```

**Lưu ý:** Email phải được xác thực OTP trước khi đăng ký.

### Đăng nhập

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "email@example.com",
  "password": "matkhau123"
}
```

**Response (200):**
```
a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6
```

**Lưu ý:** 
- Token được trả về là chuỗi UUID dạng text
- Token mặc định có hiệu lực trong 2 giờ
- Token cần được đính kèm trong header `Authorization` dưới dạng `Bearer {token}` cho các API yêu cầu xác thực

### Đăng xuất

**Endpoint:** `POST /api/auth/logout`

**Headers:**
- `Authorization`: `Bearer {token}`

**Response (200):**
```json
{
  "message": "Đăng xuất thành công"
}
```

**Lưu ý:**
- Sau khi đăng xuất, token sẽ không còn hiệu lực
- API sẽ luôn trả về thành công, ngay cả khi token không hợp lệ hoặc đã hết hạn

### Các lưu ý về xác thực

1. **Thời hạn token:**
   - Token mặc định có hiệu lực trong 2 giờ
   - Sau khi hết hạn, cần đăng nhập lại để lấy token mới

2. **Quyền hạn:**
   - Tài khoản đăng ký mặc định có quyền USER
   - Các API quản trị yêu cầu quyền ADMIN (thêm/sửa/xóa sản phẩm, danh mục...)

3. **Xử lý lỗi:**
   - **401 (Unauthorized)**: Token không hợp lệ hoặc đã hết hạn
   - **403 (Forbidden)**: Không có quyền thực hiện hành động

## API Người dùng

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

## API Phiên đăng nhập

### Lấy danh sách phiên đăng nhập

**Endpoint:** `GET /api/account/sessions`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (200):**
```json
[
  {
    "id": 1,
    "deviceType": "Desktop",
    "platform": "Windows",
    "browser": "Chrome",
    "ipAddress": "127.0.0.1",
    "lastActivity": "2023-01-01T12:30:00",
    "isCurrentSession": true,
    "isRememberMe": false
  }
]
```

### Đăng xuất khỏi một phiên

**Endpoint:** `DELETE /api/account/sessions/{sessionId}`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (204):** *Không có nội dung*

### Đăng xuất khỏi các thiết bị khác

**Endpoint:** `DELETE /api/account/sessions/other-devices`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (204):** *Không có nội dung*

### Đăng xuất khỏi tất cả thiết bị

**Endpoint:** `DELETE /api/account/sessions/all`

**Headers:**
- `Authorization`: `Bearer {token_string}`

**Response (204):** *Không có nội dung*

## API Đơn hàng

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
- `Authorization`: `Bearer {token_string}`

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

## API Sản phẩm

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

## API Danh mục

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

## API Tin tức

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

## Mã lỗi và giải thích

- `200 OK`: Yêu cầu thành công
- `201 Created`: Tạo mới tài nguyên thành công
- `204 No Content`: Xử lý thành công nhưng không có nội dung trả về
- `400 Bad Request`: Yêu cầu không hợp lệ
- `401 Unauthorized`: Không có quyền truy cập
- `403 Forbidden`: Không đủ quyền để thực hiện hành động
- `404 Not Found`: Không tìm thấy tài nguyên
- `500 Internal Server Error`: Lỗi máy chủ 