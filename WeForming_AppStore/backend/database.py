import os

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Connection string comes from the environment (see docker-compose.yml). Defaults
# to the local Postgres service. A sqlite:// URL still works (used by the
# throwaway test stack and for quick local runs).
SQLALCHEMY_DATABASE_URL = os.getenv(
    "SQLALCHEMY_DATABASE_URL",
    "postgresql+psycopg2://weforming:weforming@db:5432/weforming",
)

# check_same_thread is a SQLite-only argument.
connect_args = (
    {"check_same_thread": False} if SQLALCHEMY_DATABASE_URL.startswith("sqlite") else {}
)

engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args=connect_args)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# NOTE: tables are created in main.py (Base.metadata.create_all) *after* the
# models are imported. Calling create_all here would run before any model is
# registered on Base.metadata, so it would create nothing on a fresh database.
