echo "Loading inventory:" 
echo "$(cat test-files/inventory.json)"
curl -s http://localhost:8080/inventory/update \
-H "Content-Type: application/json" \
-d @test-files/inventory.json
echo

echo "Loading products:"
echo "$(cat test-files/products.json)"
curl -s http://localhost:8080/products/update \
-H "Content-Type: application/json" \
-d @test-files/products.json
echo
echo

echo "Listing available products:"
curl -s http://localhost:8080/products/available
echo
echo

echo "Trying to sell a product quantity with not enough supplies"
echo "$(cat test-files/sell-product-no-supplies.json)"
curl -s http://localhost:8080/products/sell \
-H "Content-Type: application/json" \
-d @test-files/sell-product-no-supplies.json
echo
echo

echo "Selling products:"
echo "$(cat test-files/sell-product.json)"
curl -s http://localhost:8080/products/sell \
-H "Content-Type: application/json" \
-d @test-files/sell-product.json
echo
echo


echo "Listing updated available products:"
curl -s http://localhost:8080/products/available
echo
