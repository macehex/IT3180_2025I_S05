-- Script SQL để cho phép owner_id NULL trong bảng apartments
-- Chạy script này trước khi sử dụng chức năng thêm căn hộ mới

-- Nếu cột tên là owner_id (theo schema hiện tại)
ALTER TABLE apartments ALTER COLUMN owner_id DROP NOT NULL;

-- Hoặc nếu cột tên là host_id (nếu bạn đã đổi tên)
-- ALTER TABLE apartments ALTER COLUMN host_id DROP NOT NULL;

