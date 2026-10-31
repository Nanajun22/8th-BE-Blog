MERGE INTO member (member_id, nickname, password, email, created_at, updated_at)
    KEY (member_id)
    VALUES (1, 'testuser', 'test1234!', 'test@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO member (member_id, nickname, password, email, created_at, updated_at)
    KEY (member_id)
    VALUES (2, 'testuser2', 'test1234!', 'test2@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);