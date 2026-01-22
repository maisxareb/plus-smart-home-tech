package ru.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.api.delivery.client.DeliveryClient;
import ru.practicum.interaction.api.order.client.OrderClient;
import ru.practicum.interaction.api.shopping.cart.dto.ShoppingCartDto;
import ru.practicum.interaction.api.warehouse.dto.*;
import ru.practicum.interaction.api.warehouse.exception.ProductLowQuantityInWarehouse;
import ru.practicum.warehouse.Warehouse;
import ru.practicum.warehouse.exception.*;
import ru.practicum.warehouse.mapper.OrderBookingMapper;
import ru.practicum.warehouse.mapper.ProductInWarehouseMapper;
import ru.practicum.warehouse.model.*;
import ru.practicum.warehouse.repository.OrderBookingRepository;
import ru.practicum.warehouse.repository.WarehouseRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final OrderBookingRepository orderBookingRepository;
    private final OrderBookingMapper orderBookingMapper;
    private final ProductInWarehouseMapper productInWarehouseMapper;

    private final OrderClient orderClient;
    private final DeliveryClient deliveryClient;

    @Override
    public ProductInWarehouseDto addNewProduct(NewProductInWarehouseRequest newProduct) {
        if (warehouseRepository.existsById(newProduct.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Продукт с id " + newProduct.getProductId() + " уже добавлен на склад!");
        }

        ProductInWarehouse entity = productInWarehouseMapper.toEntity(newProduct);
        ProductInWarehouse savedEntity = warehouseRepository.save(entity);
        return productInWarehouseMapper.toDto(savedEntity);
    }

    @Override
    public BookedProductsDto checkQuantityForCart(ShoppingCartDto shoppingCart) {
        if (shoppingCart.getProducts() == null || shoppingCart.getProducts().isEmpty()) {
            return BookedProductsDto.builder().build();
        }

        List<UUID> productIds = new ArrayList<>(shoppingCart.getProducts().keySet());
        List<ProductInWarehouse> productsInWarehouse = warehouseRepository.findAllById(productIds);

        Map<UUID, ProductInWarehouse> productMap = productsInWarehouse.stream()
                .collect(Collectors.toMap(ProductInWarehouse::getProductId, Function.identity()));

        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;

        for (Map.Entry<UUID, Integer> entry : shoppingCart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            ProductInWarehouse productInWarehouse = productMap.get(productId);

            if (productInWarehouse == null) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Товар с id " + productId + " не найден на складе!");
            }

            if (requestedQuantity > productInWarehouse.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Товара с id " + productId + " в корзине больше, чем доступно на складе!");
            }

            deliveryWeight += productInWarehouse.getWeight();
            deliveryVolume += calculateVolume(productInWarehouse);
        }

        return BookedProductsDto.builder()
                .deliveryWeight(deliveryWeight)
                .deliveryVolume(deliveryVolume)
                .build();
    }

    @Override
    @Transactional
    public void acceptProduct(AddProductToWarehouseRequest request) {
        ProductInWarehouse productInWarehouse = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductInWarehouseNotFoundException(
                        "Продукт с id " + request.getProductId() + " не найден на складе!"));

        productInWarehouse.setQuantity(productInWarehouse.getQuantity() + request.getQuantity());
        warehouseRepository.save(productInWarehouse);

        log.info("Продукт с id {} в количестве {} принят на склад!",
                productInWarehouse.getProductId(), request.getQuantity());
    }

    @Override
    public AddressDto getAddress() {
        return Warehouse.getRandomAddress();
    }

    @Override
    @Transactional
    public void shippedProducts(ShippedToDeliveryRequest request) {
        OrderBooking orderBooking = orderBookingRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotOrderBookingFound(
                        "Забронированные товары для заказа с id " + request.getOrderId() + " не найдены!"));

        orderBooking.setDeliveryId(request.getDeliveryId());
        orderBookingRepository.save(orderBooking);

        log.info("Товары для заказа с id {} переданы в доставку!", request.getOrderId());
    }

    @Override
    @Transactional
    public void returnProducts(Map<UUID, Integer> products) {
        if (products == null || products.isEmpty()) {
            log.info("Нет товаров для возврата на склад!");
            return;
        }

        List<UUID> productIds = new ArrayList<>(products.keySet());
        List<ProductInWarehouse> productsInWarehouse = warehouseRepository.findAllById(productIds);

        Map<UUID, ProductInWarehouse> productMap = productsInWarehouse.stream()
                .collect(Collectors.toMap(ProductInWarehouse::getProductId, Function.identity()));

        List<ProductInWarehouse> productsToUpdate = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            ProductInWarehouse product = productMap.get(productId);
            if (product != null) {
                product.setQuantity(product.getQuantity() + quantity);
                productsToUpdate.add(product);
            } else {
                log.warn("Продукт с id {} не найден на складе, пропускаем!", productId);
            }
        }

        if (!productsToUpdate.isEmpty()) {
            warehouseRepository.saveAll(productsToUpdate);
        }

        log.info("Товары успешно вернулись на склад!");
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProducts(AssemblyProductsForOrderRequest request) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new ProductInWarehouseNotFoundException("Нет товаров для сборки!");
        }

        List<UUID> productIds = new ArrayList<>(request.getProducts().keySet());
        List<ProductInWarehouse> productsInWarehouse = warehouseRepository.findAllById(productIds);

        Map<UUID, ProductInWarehouse> productMap = productsInWarehouse.stream()
                .collect(Collectors.toMap(ProductInWarehouse::getProductId, Function.identity()));

        double deliveryWeight = 0.0;
        double deliveryVolume = 0.0;
        boolean fragile = false;
        List<ProductInWarehouse> productsToUpdate = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : request.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            ProductInWarehouse product = productMap.get(productId);
            if (product == null) {
                throw new SpecifiedProductAlreadyInWarehouseException(
                        "Товар с id " + productId + " не найден на складе!");
            }

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductLowQuantityInWarehouse(
                        "Товара с id " + productId + " на складе меньше, чем запрашивается!");
            }

            product.setQuantity(product.getQuantity() - requestedQuantity);
            productsToUpdate.add(product);

            log.info("Остаток товара с id: {} на складе: {}.", productId, product.getQuantity());

            deliveryWeight += product.getWeight();
            deliveryVolume += calculateVolume(product);

            if (product.getFragile()) {
                fragile = true;
            }
        }

        if (!productsToUpdate.isEmpty()) {
            warehouseRepository.saveAll(productsToUpdate);
        }

        OrderBooking newOrderBooking = OrderBooking.builder()
                .products(request.getProducts())
                .deliveryWeight(deliveryWeight)
                .deliveryVolume(deliveryVolume)
                .fragile(fragile)
                .build();

        orderClient.assemblyOrder(request.getOrderId());

        OrderBooking savedOrderBooking = orderBookingRepository.save(newOrderBooking);
        return orderBookingMapper.toDto(savedOrderBooking);
    }

    private double calculateVolume(ProductInWarehouse product) {
        DimensionDto dimension = product.getDimension();
        return dimension.getHeight() * dimension.getDepth() * dimension.getWidth();
    }
}