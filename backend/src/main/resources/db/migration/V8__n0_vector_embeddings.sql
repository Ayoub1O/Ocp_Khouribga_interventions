create extension if not exists vector;

create table knowledge_chunk_embeddings (
    id bigserial primary key,
    chunk_id uuid not null unique,
    embedding vector(768) not null,
    embedding_model varchar(120) not null,
    embedding_dimension integer not null,
    date_creation timestamp not null default now(),
    date_derniere_modification timestamp not null default now(),
    constraint fk_knowledge_chunk_embeddings_chunk
        foreign key (chunk_id) references knowledge_chunks (id) on delete cascade
);

create index idx_knowledge_chunk_embeddings_chunk
    on knowledge_chunk_embeddings (chunk_id);

create index idx_knowledge_chunk_embeddings_vector
    on knowledge_chunk_embeddings
    using ivfflat (embedding vector_cosine_ops)
    with (lists = 100);
