package com.food.foodcatalogue.mapper;

import com.food.foodcatalogue.dto.FoodItemDTO;
import com.food.foodcatalogue.entity.FoodItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FoodItemMapper {


    FoodItemMapper INSTANCE = Mappers.getMapper(FoodItemMapper.class);

    FoodItem mapFoodItemDTOToFoodItem(FoodItemDTO foodItemDTO);

    FoodItemDTO mapFoodItemToFoodItemDto(FoodItem foodItem);

    List<FoodItemDTO> mapFoodItemListToFoodItemDtoList(List<FoodItem> foodItemList);


}
