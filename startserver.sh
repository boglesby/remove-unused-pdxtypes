. ./setenv.sh

gfsh start server --name=server-$1 --server-port=0 --locators=localhost[10334] --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
