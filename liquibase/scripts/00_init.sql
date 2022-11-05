-- liquibase formatted sql
-- changeset dabico:1

CREATE SEQUENCE hibernate_sequence START 1 INCREMENT 1;

CREATE TABLE "crawl_job" (
    "id" bigint PRIMARY KEY NOT NULL,
    "checkpoint" timestamp NOT NULL,
    "job_type" text NOT