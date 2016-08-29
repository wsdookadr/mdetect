CREATE TABLE gitfiles (
    filesize INT,
    path VARCHAR(1000),
    sha1 VARCHAR(40),
    gtag VARCHAR(100)
);

CREATE UNIQUE INDEX `idx_gitfiles_path_sha1_gtag` ON `gitfiles` (`gtag`,`path`,`sha1`);
CREATE INDEX        `idx_gitfiles_filesize`       ON `gitfiles` (`filesize`);
CREATE INDEX        `idx_gitfiles_path`           ON `gitfiles` (`path`);
CREATE INDEX        `idx_gitfiles_sha1`           ON `gitfiles` (`path`);
CREATE INDEX        `idx_gitfiles_gtag`           ON `gitfiles` (`path`);
