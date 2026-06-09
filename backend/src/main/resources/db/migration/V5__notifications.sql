create table notifications (
    id uuid not null,
    recipient_id uuid not null,
    type varchar(50) not null,
    title varchar(180) not null,
    message varchar(1000) not null,
    resource_type varchar(100),
    resource_id uuid,
    created_at timestamp(6) with time zone not null,
    read_at timestamp(6) with time zone,
    primary key (id),
    constraint fk_notifications_recipient foreign key (recipient_id) references user_accounts (id)
);

create index idx_notifications_recipient_read on notifications (recipient_id, read_at);
create index idx_notifications_created_at on notifications (created_at);
