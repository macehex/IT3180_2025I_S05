users{

user_id INT pk

username VARCHAR(20) UNIQUE NOT NULL

password VARCHAR(100) NOT NULL

email VARCHAR(30) UNIQUE NOT NULL

phone_number VARCHAR(15) UNIQUE

full_name VARCHAR(30)

role_id INT

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

last_login TIMESTAMP

}



roles {

role_id INT pk

role_name VARCHAR(50) UNIQUE

}



permission {

permission_id INT pk

permission_name VARCHAR(100) UNIQUE

}



role_permissions {

role_id INT pk

permission_id INT pk

}



apartments{

apartment_id INT pk

area DECIMAL(6,2)

owner_id INT NOT NULL

}



residents{

resident_id INT pk

apartment_id INT

user_id INT

full_name VARCHAR(50)

date_of_birth DATE

id_card_number VARCHAR(12)

relationship VARCHAR(10)

status VARCHAR(20) DEFAULT 'RESIDING'

move_in_date DATE

move_out_date DATE

}



invoices{

invoice_id INT pk

apartment_id INT NOT NULL

total_amount DECIMAL(10 NOT NULL,2)

due_date DATE NOT NULL

status VARCHAR(20) DEFAULT 'UNPAID'

}



invoicedetails{

invoice_detail_id INT pk

invoice_id INT NOT NULL

name VARCHAR(20) NOT NULL

amount DECIMAL(10 NOT NULL,2)

}



transactions{

transaction_id INT pk

invoice_id INT NOT NULL

payer_user_id INT NOT NULL

amount DECIMAL(10 NOT NULL,2)

transaction_date DATE

}



announcements{

ann_id INT pk

author_id INT NOT NULL

ann_title VARCHAR(50)

content TEXT NOT NULL

is_urgent BOOLEAN DEFAULT FALSE

created_at DATE

}



service_requests{

request_id INT pk

req_user_id INT NOT NULL

req_type VARCHAR(20) NOT NULL

req_title VARCHAR(50)

description TEXT NOT NULL

status VARCHAR(20) DEFAULT 'PENDING'

created_at DATE

completed_at DATE

asset_id INT

}



vehicles{

vehicle_id INT pk

resident_id INT NOT NULL

license_plate VARCHAR(15) UNIQUE NOT NULL

vehicle_type VARCHAR(20)

}



feedback{

feedback_id INT pk

user_id INT NOT NULL

content TEXT

created_at DATE

}



assets{

asset_id INT pk

asset_type VARCHAR(50)

description TEXT

location VARCHAR(20)

status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'

purchase_date DATE

initial_cost DECIMAL(12, 2)

}



maintenance_history{

maintenance_id INT pk

asset_id INT NOT NULL

maintenance_date DATE NOT NULL

description TEXT

cost DECIMAL(10, 2)

performed_by VARCHAR(100)

}



update_requests{

request_id INT pk

user_id INT NOT NULL

requested_data JSON NOT NULL

status VARCHAR(20) DEFAULT 'PENDING' NOT NULL

requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

reviewed_by INT

reviewed_at TIMESTAMP

review_notes TEXT

}



// Relationships

users.user_id - residents.user_id// "has"

apartments.apartment_id < residents.apartment_id// "contains"

users.user_id < apartments.owner_id// "owns"

apartments.apartment_id < invoices.apartment_id// "has"

users.user_id < invoices.invoice_id// "receives"

users.user_id < transactions.payer_user_id// "makes"

invoices.invoice_id - transactions.invoice_id// "is paid by"

invoices.invoice_id < invoicedetails.invoice_id// "is composed of"

users.user_id < announcements.author_id// "authors"

users.user_id < service_requests.req_user_id// "creates"

residents.resident_id < vehicles.resident_id// "owns"

users.user_id < feedback.user_id// "gives"

role_permissions.role_id - roles.role_id

role_permissions.permission_id - permission.permission_id

users.role_id - roles.role_id

assets.asset_id < maintenance_history.asset_id // "has"

assets.asset_id < service_requests.asset_id // "is subject of"

update_requests.user_id > users.user_id // "submitted by"

update_requests.reviewed_by > users.user_id // "reviewed by"