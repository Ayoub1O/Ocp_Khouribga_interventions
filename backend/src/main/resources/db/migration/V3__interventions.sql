create table interventions (
    id uuid not null,
    ticket_id uuid not null,
    technicien_id uuid not null,
    statut varchar(30) not null,
    date_debut_prevue timestamp(6) with time zone not null,
    date_fin_prevue timestamp(6) with time zone not null,
    date_debut_reelle timestamp(6) with time zone,
    date_fin_reelle timestamp(6) with time zone,
    lieu varchar(255) not null,
    rapport varchar(4000),
    date_creation timestamp(6) with time zone not null,
    date_derniere_modification timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_interventions_ticket foreign key (ticket_id) references tickets (id),
    constraint fk_interventions_technicien foreign key (technicien_id) references user_accounts (id),
    constraint ck_interventions_date_prevue check (date_fin_prevue > date_debut_prevue)
);

create index idx_interventions_ticket on interventions (ticket_id);
create index idx_interventions_technicien on interventions (technicien_id);
create index idx_interventions_status on interventions (statut);
create index idx_interventions_planning on interventions (date_debut_prevue, date_fin_prevue);
