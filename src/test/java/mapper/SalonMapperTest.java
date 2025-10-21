package mapper;

import com.salonized.dto.Salon;
import training.salonzied.dao.entities.SalonEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import training.salonzied.mapper.SalonMapper;
import util.TestData;

import static org.junit.jupiter.api.Assertions.*;


class SalonMapperTest {

    private final SalonMapper mapper = Mappers.getMapper(SalonMapper.class);
    @Test
    void mapSalonRequestToSalon() {

        SalonEntity entity = mapper.toEntity(TestData.getCreateSalonRequest());
        assertNotNull(entity);
        assertEquals("Salon", entity.getName());
        assertEquals("Bacau", entity.getAddress().getCity());
        assertEquals("12B", entity.getAddress().getHouseNumber());
        assertEquals("a12", entity.getAddress().getPostalBox());
        assertEquals("1080", entity.getAddress().getPostcode());
        assertNotNull(entity.getPublicId());
    }

    @Test
    void mapSalonEntityToSalonDto(){
        Salon salon = mapper.entityToSalonDto(TestData.getSalonEntity());
        assertNotNull(salon);
        assertEquals("Salon Prestige", salon.getName());
        assertEquals("Bacau", salon.getAddress().getCity());
        assertEquals("12B", salon.getAddress().getHouseNumber());
        assertEquals("a12", salon.getAddress().getPostalBox());
        assertEquals("1080", salon.getAddress().getPostcode());
        assertEquals("b7a6c6f4-9d5a-4f91-8e71-39ef99e8c9c3", salon.getPublicId());
    }

}