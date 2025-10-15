--
-- PostgreSQL database dump
--

\restrict 6pgAjKYb562kXQkZ0ApFkVFXTCfkY7h0OVmulZMeHGoAOrFaOTLUdTO36AWM8uN

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: cuppa_user
--

COPY public.users (id, username, email, password_hash, first_name, last_name, avatar_url, phone, is_online, last_seen, created_at, updated_at) FROM stdin;
8	ggkam	ggkam@example.com	$2a$10$FR8SnPLmWhxp.VPXXej8guNNgFTz0r7ZDbs/GzmzqfkurIamZXd9.	Test	User	\N	\N	f	2025-10-14 20:07:02.33956+07	2025-10-14 20:07:02.33956+07	2025-10-14 20:07:02.33956+07
9	ggkamchek	ggkamcheck@example.com	$2a$10$zcQuVb4ZOkwW3X4Eb6tSzOqrCSCqjeby6I4W8gFpdjhip1OWnHRKu	Test	User	\N	\N	f	2025-10-14 20:19:46.062866+07	2025-10-14 20:19:46.062866+07	2025-10-14 20:19:46.062866+07
10	Tosya	Tosya@example.com	$2a$10$wJKqzEbHpzg68YpT.70sWej2bSFBA4u0EDnI3zrs4.2cMD5wR0jtS	Test	User	\N	\N	f	2025-10-14 20:58:24.998017+07	2025-10-14 20:58:24.998017+07	2025-10-14 20:58:24.998017+07
11	Waleria	Waleria@example.com	$2a$10$uWjCofQqOVsE/NxmiPZUMuACqzVfYa932gqdXbMaWQzpBsOCDtoRy	Test	User	\N	\N	f	2025-10-14 22:50:25.381792+07	2025-10-14 22:50:25.381792+07	2025-10-14 22:50:25.381792+07
\.


--
-- Data for Name: chat_rooms; Type: TABLE DATA; Schema: public; Owner: cuppa_user
--

COPY public.chat_rooms (id, name, type, created_by, created_at, updated_at, last_message_at, last_message_text, last_message_sender_id, avatar_url, description, is_active, max_participants) FROM stdin;
\.


--
-- Data for Name: chat_participants; Type: TABLE DATA; Schema: public; Owner: cuppa_user
--

COPY public.chat_participants (id, chat_id, user_id, joined_at, role, is_active, last_read_at) FROM stdin;
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: cuppa_user
--

COPY public.messages (id, chat_id, sender_id, content, message_type, sent_at, delivered_at, read_at, reply_to_message_id, is_edited, edited_at) FROM stdin;
\.


--
-- Name: chat_participants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cuppa_user
--

SELECT pg_catalog.setval('public.chat_participants_id_seq', 12, true);


--
-- Name: chat_rooms_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cuppa_user
--

SELECT pg_catalog.setval('public.chat_rooms_id_seq', 4, true);


--
-- Name: messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cuppa_user
--

SELECT pg_catalog.setval('public.messages_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: cuppa_user
--

SELECT pg_catalog.setval('public.users_id_seq', 11, true);


--
-- PostgreSQL database dump complete
--

\unrestrict 6pgAjKYb562kXQkZ0ApFkVFXTCfkY7h0OVmulZMeHGoAOrFaOTLUdTO36AWM8uN

