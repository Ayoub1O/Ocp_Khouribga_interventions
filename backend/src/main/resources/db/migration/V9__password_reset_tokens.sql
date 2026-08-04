create table password_reset_tokens (
    id uuid not null,
    token_hash varchar(128) not null,
    user_id uuid not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    consumed_at timestamp(6) with time zone,
    primary key (id),
    constraint uk_password_reset_tokens_hash unique (token_hash),
    constraint fk_password_reset_tokens_user foreign key (user_id) references user_accounts (id)
);

create index idx_password_reset_tokens_user on password_reset_tokens (user_id);
