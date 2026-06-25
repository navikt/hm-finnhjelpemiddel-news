CREATE TABLE IF NOT EXISTS news_tags (
    tag_id UUID NOT NULL REFERENCES tags(id),
    news_id UUID NOT NULL REFERENCES news(id) ON DELETE CASCADE,
    PRIMARY KEY (tag_id, news_id)
    );


