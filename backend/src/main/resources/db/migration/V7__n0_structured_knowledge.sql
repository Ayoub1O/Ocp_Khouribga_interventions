alter table knowledge_chunks
    add column section_type varchar(30) not null default 'PROCEDURE';

create table knowledge_sections (
    id uuid not null,
    article_id uuid not null,
    type varchar(30) not null,
    titre varchar(180) not null,
    contenu varchar(4000) not null,
    ordre integer not null,
    primary key (id),
    constraint fk_knowledge_sections_article foreign key (article_id) references knowledge_articles (id)
);

create index idx_knowledge_sections_article on knowledge_sections (article_id);
create index idx_knowledge_sections_type on knowledge_sections (type);

insert into knowledge_sections (
    id,
    article_id,
    type,
    titre,
    contenu,
    ordre
) values
(
    '30000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    'PROCEDURE',
    'Procedure',
    '1. Verifiez que la connexion Internet fonctionne. 2. Fermez puis relancez le client VPN. 3. Verifiez que le poste utilise le bon profil VPN. 4. Si une erreur 809 persiste, escaladez vers N1 avec le code exact.',
    0
),
(
    '30000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'PROCEDURE',
    'Procedure',
    '1. Verifiez que le clavier utilise la bonne langue. 2. Essayez la connexion sur le portail interne. 3. Si le compte est verrouille ou si le mot de passe est oublie, demandez une reinitialisation via le support N1.',
    0
),
(
    '30000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003',
    'PROCEDURE',
    'Procedure',
    '1. Verifiez la connexion Internet. 2. Redemarrez Outlook. 3. Controlez que la boite n''est pas pleine. 4. Si le probleme concerne SMTP, IMAP ou un message d''erreur precis, escaladez vers N1.',
    0
),
(
    '30000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000004',
    'PROCEDURE',
    'Procedure',
    '1. Verifiez que l''imprimante est allumee. 2. Controlez papier et toner. 3. Redemarrez la file d''impression. 4. Si le voyant materiel indique une panne, escaladez vers N2.',
    0
);
