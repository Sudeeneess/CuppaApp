CREATE INDEX IF NOT EXISTS idx_chat_rooms_last_message_at ON chat_rooms USING btree (last_message_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_chat_id_sent_at ON messages USING btree (chat_id, sent_at DESC);
