package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureMockMvc
public class UserPointHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    private PointService pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = Mockito.mock(UserPointTable.class);
        pointHistoryTable = Mockito.mock(PointHistoryTable.class);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("포인트 내역 조회")
    void getUserPointHistory() {
        // given
        long userId = 10L;
        List<PointHistory> userPointHistory = Arrays.asList(
          new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
          new PointHistory(2L, userId, 700L, TransactionType.USE, System.currentTimeMillis()),
          new PointHistory(2L, userId, 300L, TransactionType.CHARGE, System.currentTimeMillis())
        );
        Mockito.when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(userPointHistory);

        // when
        CommonResponse response = pointService.getUserPointHistories(userId);
        List<PointHistory> pointHistoryList = (List<PointHistory>) response.getData();

        // then
        assertEquals(pointHistoryList.size(), 3);
        assertEquals(pointHistoryList.get(0).amount(), 1000L);
        assertEquals(pointHistoryList.get(0).type(), TransactionType.CHARGE);

        assertEquals(pointHistoryList.get(1).amount(), 700L);
        assertEquals(pointHistoryList.get(1).type(), TransactionType.USE);

        assertEquals(pointHistoryList.get(2).amount(), 300L);
        assertEquals(pointHistoryList.get(2).type(), TransactionType.CHARGE);
    }

    @Test
    @DisplayName("포인트 내역이 없는 유저 조회")
    void getUserPointEmptyHistory() {
        // given
        long userId = 10L;
        Mockito.when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(Collections.emptyList());

        // when
        CommonResponse response = pointService.getUserPointHistories(userId);
        List<PointHistory> pointHistoryList = (List<PointHistory>) response.getData();

        // then
        assertEquals(pointHistoryList, Collections.emptyList());
    }
}
