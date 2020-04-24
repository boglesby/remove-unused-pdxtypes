. ./setenv.sh

# Start locator
echo "Starting locator"
gfsh start locator --name=locator

# Start 3 servers
for (( i=1; i<=3; i++ ))
do
	echo "Starting server $i"
	./startserver.sh $i &
done
wait
