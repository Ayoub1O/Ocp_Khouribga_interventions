alter table user_accounts
    add column email_verified boolean not null default false;

update user_accounts
set email_verified = true
where role = 'ADMIN';

create table email_verification_tokens (
    id uuid not null,
    token_hash varchar(128) not null,
    user_id uuid not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    consumed_at timestamp(6) with time zone,
    primary key (id),
    constraint uk_email_verification_tokens_hash unique (token_hash),
    constraint fk_email_verification_tokens_user foreign key (user_id) references user_accounts (id)
);

create table user_invitations (
    id uuid not null,
    token_hash varchar(128) not null,
    user_id uuid not null,
    invited_role varchar(30) not null,
    invited_by_id uuid not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    accepted_at timestamp(6) with time zone,
    revoked_at timestamp(6) with time zone,
    primary key (id),
    constraint uk_user_invitations_hash unique (token_hash),
    constraint fk_user_invitations_user foreign key (user_id) references user_accounts (id),
    constraint fk_user_invitations_invited_by foreign key (invited_by_id) references user_accounts (id)
);

create index idx_email_verification_tokens_user on email_verification_tokens (user_id);
create index idx_user_invitations_user on user_invitations (user_id);
create index idx_user_invitations_invited_by on user_invitations (invited_by_id);
