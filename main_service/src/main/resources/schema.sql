DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

CREATE TABLE users (
  user_id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  user_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (user_id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email),
  CONSTRAINT NOT_EMPTY_EMAIL_AND_NAME CHECK(email <> '' AND user_name <> '')
);

CREATE TABLE categories (
  category_id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  category_name VARCHAR(50) NOT NULL,
  CONSTRAINT pk_categories PRIMARY KEY (category_id),
  CONSTRAINT UQ_CATEGORY_NAME UNIQUE (category_name)
);

CREATE TABLE events (
  event_id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  event_title VARCHAR(120) NOT NULL,
  event_description VARCHAR(7000) NOT NULL,
  event_annotation VARCHAR(2000) NOT NULL,
  event_date TIMESTAMP NOT NULL,
  event_state VARCHAR(20) NOT NULL,
  initiator_id INTEGER NOT NULL,
  category_id INTEGER NOT NULL,
  geo_latitude REAL,
  geo_longitude REAL,
  participant_limit INTEGER CHECK(participant_limit >= 0),
  confirmed_requests INTEGER CHECK(confirmed_requests >= 0),
  paid BOOLEAN,
  request_moderation BOOLEAN,
  created_on timestamp,
  published_on timestamp,
  CONSTRAINT pk_events PRIMARY KEY (event_id),
  CONSTRAINT fk_initiator_id FOREIGN KEY (initiator_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_category_id FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT,
  CONSTRAINT UQ_INITIATOR_TITLE_EVENT_DATE UNIQUE (initiator_id, event_title, event_date)
);

CREATE TABLE requests (
  request_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  requester_id INTEGER NOT NULL,
  event_id INTEGER NOT NULL,
  created_on timestamp,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT pk_requests PRIMARY KEY (request_id),
  CONSTRAINT fk_requester_id FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_event_id FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
  CONSTRAINT UQ_REQUESTER_AND_EVENT_ID UNIQUE (requester_id, event_id),
  CONSTRAINT NOT_EMPTY_STATUS CHECK(status <> '')
);