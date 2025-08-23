package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestPointController {

    @Autowired
    private MockMvc mockMvc;

    private PointService pointService;
    private UserPointTable userPointTable;

    @BeforeEach
    void setUp() {
        userPointTable = Mockito.mock(UserPointTable.class);
        PointHistoryTable pointHistoryTable = Mockito.mock(PointHistoryTable.class);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("유효하지 않은 id로 조회 시 Bad Request 응답")
    void givenInvalidId_whenRequestPoint_thenReturnsBadRequest() throws Exception {
        // given
        long invalidId = 0L;

        // when & then
        mockMvc.perform(get("/point/" + invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("0원으로 포인트 충전 요청 시 실패")
    void givenZeroUserPoint_whenChargeUserPoint() {
        // given
        long userId = 1L;
        long amount = 0L;
        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 1000, System.currentTimeMillis()));
        Mockito.when(userPointTable.insertOrUpdate(userId, 1000L)).thenReturn(new UserPoint(userId, 1000, System.currentTimeMillis()));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> pointService.chargeUserPoint(userId, amount));
    }
}
