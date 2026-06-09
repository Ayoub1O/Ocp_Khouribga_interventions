create table spare_parts (
    id uuid not null,
    reference varchar(80) not null,
    nom varchar(180) not null,
    description varchar(1000),
    quantite_disponible integer not null,
    seuil_alerte integer not null,
    actif boolean not null,
    date_creation timestamp(6) with time zone not null,
    date_derniere_modification timestamp(6) with time zone not null,
    primary key (id),
    constraint uk_spare_parts_reference unique (reference),
    constraint ck_spare_parts_quantite_non_negative check (quantite_disponible >= 0),
    constraint ck_spare_parts_seuil_non_negative check (seuil_alerte >= 0)
);

create table stock_movements (
    id uuid not null,
    piece_id uuid not null,
    type varchar(30) not null,
    quantite integer not null,
    intervention_id uuid,
    technicien_id uuid not null,
    commentaire varchar(1000) not null,
    date_mouvement timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_stock_movements_piece foreign key (piece_id) references spare_parts (id),
    constraint fk_stock_movements_intervention foreign key (intervention_id) references interventions (id),
    constraint fk_stock_movements_technicien foreign key (technicien_id) references user_accounts (id),
    constraint ck_stock_movements_quantite_non_negative check (quantite >= 0)
);

create index idx_spare_parts_reference on spare_parts (reference);
create index idx_spare_parts_active on spare_parts (actif);
create index idx_stock_movements_piece on stock_movements (piece_id);
create index idx_stock_movements_intervention on stock_movements (intervention_id);
create index idx_stock_movements_date on stock_movements (date_mouvement);
