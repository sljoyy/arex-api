package com.arextest.web.core.business;

import com.arextest.web.core.repository.mongo.ReportPlanStatisticRepositoryImpl;
import com.arextest.web.model.contract.contracts.QueryPlanStatisticRequestType;
import com.arextest.web.model.contract.contracts.QueryPlanStatisticResponseType;
import com.arextest.web.model.contract.contracts.QueryPlanStatisticsRequestType;
import com.arextest.web.model.contract.contracts.QueryPlanStatisticsResponseType;
import com.arextest.web.model.contract.contracts.common.CaseCount;
import com.arextest.web.model.contract.contracts.common.PlanStatistic;
import com.arextest.web.model.dto.ReportPlanStatisticDto;
import com.arextest.web.model.mapper.PlanMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryPlanStatisticsService {

  @Resource
  private ReportPlanStatisticRepositoryImpl planStatisticRepository;

  @Autowired
  private CaseCountService caseCountService;

  public QueryPlanStatisticsResponseType planStatistics(QueryPlanStatisticsRequestType request) {
    QueryPlanStatisticsResponseType response = new QueryPlanStatisticsResponseType();

    Pair<List<ReportPlanStatisticDto>, Long> result = planStatisticRepository.pageQueryPlanStatistic(
        request);
    List<ReportPlanStatisticDto> reportPlanStatisticDtoList = result.getLeft();
    Long totalCount = result.getRight();
    response.setTotalCount(totalCount);

    List<String> planIds =
        reportPlanStatisticDtoList.stream().map(ReportPlanStatisticDto::getPlanId)
            .collect(Collectors.toList());

    Map<String, CaseCount> caseCountMap = caseCountService.calculateCaseCountsByPlanIds(planIds);
    for (ReportPlanStatisticDto plan : reportPlanStatisticDtoList) {
      CaseCount caseCount = caseCountMap.get(plan.getPlanId());
      if (caseCount == null) {
        continue;
      }
      plan.setTotalCaseCount(caseCount.getTotalCaseCount());
      plan.setErrorCaseCount(caseCount.getErrorCaseCount());
      plan.setSuccessCaseCount(caseCount.getSuccessCaseCount());
      plan.setFailCaseCount(caseCount.getFailCaseCount());
      plan.setWaitCaseCount(caseCount.getTotalCaseCount() - caseCount.getReceivedCaseCount());
      plan.setTotalOperationCount(caseCount.getTotalOperationCount());
      plan.setSuccessOperationCount(caseCount.getSuccessOperationCount());
    }

    List<PlanStatistic> planStatistics =
        reportPlanStatisticDtoList.stream().map(PlanMapper.INSTANCE::contractFromDto)
            .collect(Collectors.toList());
    response.setPlanStatisticList(planStatistics);
    return response;
  }

  public QueryPlanStatisticResponseType planStatistic(QueryPlanStatisticRequestType request) {
    QueryPlanStatisticResponseType response = new QueryPlanStatisticResponseType();
    ReportPlanStatisticDto reportPlanStatisticDto = planStatisticRepository.findByPlanId(request.getPlanId());
    if (reportPlanStatisticDto == null) {
      return response;
    }
    Map<String, CaseCount> caseCountMap = caseCountService.calculateCaseCountsByPlanIds(Collections.singletonList(request.getPlanId()));
    if (caseCountMap.isEmpty() || !caseCountMap.containsKey(request.getPlanId())) {
      return response;
    }
    CaseCount caseCount = caseCountMap.get(request.getPlanId());
    reportPlanStatisticDto.setTotalCaseCount(caseCount.getTotalCaseCount());
    reportPlanStatisticDto.setErrorCaseCount(caseCount.getErrorCaseCount());
    reportPlanStatisticDto.setSuccessCaseCount(caseCount.getSuccessCaseCount());
    reportPlanStatisticDto.setFailCaseCount(caseCount.getFailCaseCount());
    reportPlanStatisticDto.setWaitCaseCount(caseCount.getTotalCaseCount() - caseCount.getReceivedCaseCount());
    reportPlanStatisticDto.setTotalOperationCount(caseCount.getTotalOperationCount());
    reportPlanStatisticDto.setSuccessOperationCount(caseCount.getSuccessOperationCount());
    PlanStatistic planStatistic = PlanMapper.INSTANCE.contractFromDto(reportPlanStatisticDto);
    response.setPlanStatistic(planStatistic);
    return response;
  }
}
