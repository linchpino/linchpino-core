CREATE USER linchpinodbuser;
GRANT ALL ON SCHEMA public TO linchpinodbuser;
CREATE DATABASE $currentbranches;
GRANT ALL PRIVILEGES ON DATABASE $currentbranches TO linchpinodbuser;
