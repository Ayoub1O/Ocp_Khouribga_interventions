create table knowledge_articles (
    id uuid not null,
    titre varchar(180) not null,
    categorie varchar(40) not null,
    contenu varchar(4000) not null,
    mots_cles varchar(1000) not null,
    source_type varchar(30) not null,
    source_nom varchar(255),
    actif boolean not null,
    version integer not null,
    date_creation timestamp(6) with time zone not null,
    date_derniere_modification timestamp(6) with time zone not null,
    primary key (id)
);

create table chatbot_sessions (
    id uuid not null,
    demandeur_id uuid not null,
    statut varchar(30) not null,
    categorie_detectee varchar(40),
    ticket_id uuid,
    date_creation timestamp(6) with time zone not null,
    date_fermeture timestamp(6) with time zone,
    primary key (id),
    constraint fk_chatbot_sessions_demandeur foreign key (demandeur_id) references user_accounts (id),
    constraint fk_chatbot_sessions_ticket foreign key (ticket_id) references tickets (id)
);

create table knowledge_chunks (
    id uuid not null,
    article_id uuid not null,
    contenu varchar(4000) not null,
    mots_cles varchar(1000) not null,
    actif boolean not null,
    ordre integer not null,
    primary key (id),
    constraint fk_knowledge_chunks_article foreign key (article_id) references knowledge_articles (id)
);

create table chatbot_messages (
    id uuid not null,
    session_id uuid not null,
    auteur varchar(30) not null,
    contenu varchar(4000) not null,
    sources_utilisees varchar(2000),
    confidence_score float(53),
    date_creation timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_chatbot_messages_session foreign key (session_id) references chatbot_sessions (id)
);

create index idx_knowledge_articles_category on knowledge_articles (categorie);
create index idx_knowledge_articles_active on knowledge_articles (actif);
create index idx_knowledge_chunks_article on knowledge_chunks (article_id);
create index idx_knowledge_chunks_active on knowledge_chunks (actif);
create index idx_chatbot_sessions_demandeur on chatbot_sessions (demandeur_id);
create index idx_chatbot_sessions_status on chatbot_sessions (statut);
create index idx_chatbot_messages_session on chatbot_messages (session_id);
create index idx_chatbot_messages_date on chatbot_messages (date_creation);

insert into knowledge_articles (
    id,
    titre,
    categorie,
    contenu,
    mots_cles,
    source_type,
    source_nom,
    actif,
    version,
    date_creation,
    date_derniere_modification
) values
(
    '10000000-0000-0000-0000-000000000001',
    'Connexion VPN - controles de base',
    'RESEAU',
    '1. Verifiez que la connexion Internet fonctionne. 2. Fermez puis relancez le client VPN. 3. Verifiez que le poste utilise le bon profil VPN. 4. Si une erreur 809 persiste, escaladez vers N1 avec le code exact.',
    'vpn,erreur 809,connexion,internet,reseau',
    'SYSTEME',
    'bootstrap',
    true,
    1,
    now(),
    now()
),
(
    '10000000-0000-0000-0000-000000000002',
    'Mot de passe ou acces bloque',
    'COMPTE_ACCES',
    '1. Verifiez que le clavier utilise la bonne langue. 2. Essayez la connexion sur le portail interne. 3. Si le compte est verrouille ou si le mot de passe est oublie, demandez une reinitialisation via le support N1.',
    'mot de passe,compte,login,acces,authentification',
    'SYSTEME',
    'bootstrap',
    true,
    1,
    now(),
    now()
),
(
    '10000000-0000-0000-0000-000000000003',
    'Outlook - reception et envoi email',
    'EMAIL',
    '1. Verifiez la connexion Internet. 2. Redemarrez Outlook. 3. Controlez que la boite n''est pas pleine. 4. Si le probleme concerne SMTP, IMAP ou un message d''erreur precis, escaladez vers N1.',
    'outlook,email,mail,smtp,boite',
    'SYSTEME',
    'bootstrap',
    true,
    1,
    now(),
    now()
),
(
    '10000000-0000-0000-0000-000000000004',
    'Imprimante indisponible',
    'IMPRIMANTE',
    '1. Verifiez que l''imprimante est allumee. 2. Controlez papier et toner. 3. Redemarrez la file d''impression. 4. Si le voyant materiel indique une panne, escaladez vers N2.',
    'imprimante,impression,scanner,papier,toner',
    'SYSTEME',
    'bootstrap',
    true,
    1,
    now(),
    now()
);

insert into knowledge_chunks (
    id,
    article_id,
    contenu,
    mots_cles,
    actif,
    ordre
) values
(
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    '1. Verifiez que la connexion Internet fonctionne. 2. Fermez puis relancez le client VPN. 3. Verifiez que le poste utilise le bon profil VPN. 4. Si une erreur 809 persiste, escaladez vers N1 avec le code exact.',
    'vpn,erreur 809,connexion,internet,reseau',
    true,
    0
),
(
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    '1. Verifiez que le clavier utilise la bonne langue. 2. Essayez la connexion sur le portail interne. 3. Si le compte est verrouille ou si le mot de passe est oublie, demandez une reinitialisation via le support N1.',
    'mot de passe,compte,login,acces,authentification',
    true,
    0
),
(
    '20000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003',
    '1. Verifiez la connexion Internet. 2. Redemarrez Outlook. 3. Controlez que la boite n''est pas pleine. 4. Si le probleme concerne SMTP, IMAP ou un message d''erreur precis, escaladez vers N1.',
    'outlook,email,mail,smtp,boite',
    true,
    0
),
(
    '20000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000004',
    '1. Verifiez que l''imprimante est allumee. 2. Controlez papier et toner. 3. Redemarrez la file d''impression. 4. Si le voyant materiel indique une panne, escaladez vers N2.',
    'imprimante,impression,scanner,papier,toner',
    true,
    0
);
