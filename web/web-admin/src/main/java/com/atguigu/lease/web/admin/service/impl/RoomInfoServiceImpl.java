package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private RoomAttrValueMapper roomAttrValueMapper;

    @Autowired
    private RoomFacilityMapper roomFacilityMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;


    @Override
    public void saveOrUpdateRoomInfo(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);

        // 若为更新操作
        if(isUpdate){
            // 1.删除原本的图片
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphQueryWrapper.eq(GraphInfo::getId,roomSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            // 2.删除原本属性
            LambdaQueryWrapper<RoomAttrValue> AttrQueryWrapper = new LambdaQueryWrapper<>();
            AttrQueryWrapper.eq(RoomAttrValue::getRoomId,roomSubmitVo.getId());
            roomAttrValueService.remove(AttrQueryWrapper);

            // 3.删除配套信息
            LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(RoomFacility::getRoomId,roomSubmitVo.getId());
            roomFacilityService.remove(facilityQueryWrapper);

            //4.删除标签信息
            LambdaQueryWrapper<RoomLabel> LabelQueryWrapper = new LambdaQueryWrapper<>();
            LabelQueryWrapper.eq(RoomLabel::getRoomId,roomSubmitVo.getId());
            roomLabelService.remove(LabelQueryWrapper);

            // 5.删除支付方式信息
            LambdaQueryWrapper<RoomPaymentType> paymentTypeQueryWrapper = new LambdaQueryWrapper<>();
            paymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId,roomSubmitVo.getId());
            roomPaymentTypeService.remove(paymentTypeQueryWrapper);

            // 6.删除租期列表
            LambdaQueryWrapper<RoomLeaseTerm> termLambdaQueryWrapper = new LambdaQueryWrapper<>();
            termLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId,roomSubmitVo.getId());
            roomLeaseTermService.remove(termLambdaQueryWrapper);
        }

        // 1.添加图片信息
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfos = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setName(graphVo.getName());
                graphInfos.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfos);
        }

        // 2.添加属性信息
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIds)){
            ArrayList<RoomAttrValue> roomAttrValues = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrvalue = RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(attrValueId).build();
                roomAttrValues.add(roomAttrvalue);
            }
            roomAttrValueService.saveBatch(roomAttrValues);
        }

        // 3.添加配套信息
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilities = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(facilityInfoId).build();
                roomFacilities.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }

        // 4.添加标签信息
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabels = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(labelInfoId).build();
                roomLabels.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabels);
        }

        //5.保存新的paymentTypeList
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)) {
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(paymentTypeId).build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }

        //6.保存新的leaseTermList
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(leaseTermId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

    }

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> roomItemVoPage, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(roomItemVoPage,queryVo);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {

        // 1.查询roominfo
        RoomInfo roomInfo = roomInfoMapper.selectById(id);

        // 2.查询ApartmentInfo
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        // 3.查询图片信息
        List<GraphVo> graphVos = graphInfoMapper.selectListByItemtypeAndId(ItemType.ROOM, id);

        // 4.查询属性信息
        List<AttrValueVo> attrValueVos = roomAttrValueMapper.selectListByRoomId(id);

        // 5.查询配套信息
        List<FacilityInfo> facilityInfos = roomFacilityMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);


        RoomDetailVo roomDetailVo = new RoomDetailVo();

        BeanUtils.copyProperties(roomInfo,roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVos);
        roomDetailVo.setAttrValueVoList(attrValueVos);
        roomDetailVo.setFacilityInfoList(facilityInfos);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);

        return roomDetailVo;
    }

    @Override
    public void removeRoomByid(Long id) {
        //1.删除RoomInfo
        super.removeById(id);

        //2.删除graphInfoList
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);

        //3.删除attrValueList
        LambdaQueryWrapper<RoomAttrValue> attrQueryWrapper = new LambdaQueryWrapper<>();
        attrQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrQueryWrapper);

        //4.删除facilityInfoList
        LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityQueryWrapper);

        //5.删除labelInfoList
        LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelQueryWrapper);

        //6.删除paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
        paymentQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentQueryWrapper);

        //7.删除leaseTermList
        LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
        termQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(termQueryWrapper);
    }
}




