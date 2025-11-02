package com.store.common.commands;

import com.store.common.dto.ProductStockDTO; // NUEVO IMPORT
import java.util.List; // NUEVO IMPORT
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStockCommand {
    
    private UUID orderId;
    private String reason; 
    
    private List<ProductStockDTO> products; 
}