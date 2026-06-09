create table user_accounts (
    id uuid not null,
    nom varchar(100) not null,
    prenom varchar(100) not null,
    email varchar(180) not null,
    password_hash varchar(255) not null,
    role varchar(30) not null,
    actif boolean not null,
    date_creation timestamp(6) with time zone not null,
    primary key (id),
    constraint uk_user_accounts_email unique (email)
);

create table tickets (
    id uuid not null,
    reference varchar(40) not null,
    titre varchar(180) not null,
    description varchar(4000) not null,
    categorie varchar(40) not null,
    priorite varchar(30) not null,
    statut varchar(30) not null,
    niveau_courant varchar(10) not null,
    demandeur_id uuid not null,
    technicien_assigne_id uuid,
    date_creation timestamp(6) with time zone not null,
    date_derniere_modification timestamp(6) with time zone not null,
    date_resolution timestamp(6) with time zone,
    date_cloture timestamp(6) with time zone,
    primary key (id),
    constraint uk_tickets_reference unique (reference),
    constraint fk_tickets_demandeur foreign key (demandeur_id) references user_accounts (id),
    constraint fk_tickets_technicien foreign key (technicien_assigne_id) references user_accounts (id)
);

create table ticket_events (
    id uuid not null,
    ticket_id uuid not null,
    acteur_id uuid,
    type varchar(50) not null,
    commentaire varchar(2000),
    date_evenement timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_ticket_events_ticket foreign key (ticket_id) references tickets (id),
    constraint fk_ticket_events_acteur foreign key (acteur_id) references user_accounts (id)
);

create table refresh_tokens (
    id uuid not null,
    token_hash varchar(128) not null,
    user_id uuid not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    revoked_at timestamp(6) with time zone,
    primary key (id),
    constraint uk_refresh_tokens_hash unique (token_hash),
    constraint fk_refresh_tokens_user foreign key (user_id) references user_accounts (id)
);

create index idx_tickets_status_level on tickets (statut, niveau_courant);
create index idx_tickets_demandeur on tickets (demandeur_id);
create index idx_tickets_technicien on tickets (technicien_assigne_id);
create index idx_ticket_events_ticket on ticket_events (ticket_id);
create index idx_ticket_events_date on ticket_events (date_evenement);
create index idx_refresh_tokens_user on refresh_tokens (user_id);
