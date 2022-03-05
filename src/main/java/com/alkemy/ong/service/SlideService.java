package com.alkemy.ong.service;

import com.alkemy.ong.domain.Slide;
import com.alkemy.ong.exception.OrganizationNotFoundException;
import com.alkemy.ong.exception.SlideNotFoundException;
import com.alkemy.ong.mapper.OrganizationMapper;
import com.alkemy.ong.mapper.SlideMapper;
import com.alkemy.ong.repository.OrganizationRepository;
import com.alkemy.ong.repository.SlideRepository;
import com.alkemy.ong.repository.model.OrganizationModel;
import com.alkemy.ong.repository.model.SlideModel;
import com.alkemy.ong.util.Base64DecodedMultiPartFile;
import java.util.Base64;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public class SlideService {

    private final SlideRepository slideRepository;
    private final AmazonService amazonService;
    private final OrganizationRepository organizationRepository;

    public SlideService(SlideRepository slideRepository, AmazonService amazonService, OrganizationRepository organizationRepository) {
        this.slideRepository = slideRepository;
        this.amazonService = amazonService;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public Slide slideDetails(Long idSlide) throws SlideNotFoundException {
        Optional<SlideModel> slideModel = slideRepository.findById(idSlide);
        if (!slideModel.isEmpty()) {
            Slide slide = Slide.builder()
                    .image(slideModel.get().getImage())
                    .organizationOrder(slideModel.get().getOrganizationOrder())
                    .text(slideModel.get().getText())
                    .build();
            return slide;
        } else {
            throw new SlideNotFoundException(String.format("Slide with id: %s not found", idSlide));
        }
    }

    @Transactional
    public void deleteSlide(Long idSlide) throws SlideNotFoundException {
        Optional<SlideModel> slide = slideRepository.findById(idSlide);
        if (!slide.isEmpty()) {
            slideRepository.delete(slide.get());
        } else {
            throw new SlideNotFoundException(String.format("Slide with id: %s not found", idSlide));
        }
    }

    @Transactional
    public Slide createSlide(Slide slide) {
        SlideModel slideModel = SlideMapper.mapDomainToModel(slide);
        Optional<OrganizationModel> organizationModelOptional = organizationRepository.findById(slideModel.getOrganization().getId());
        if (organizationModelOptional.isEmpty()) {
            throw new OrganizationNotFoundException(String.format("Organization with ID: %s not found", slideModel.getOrganization().getId()));
        }
        slideModel.setOrganization(OrganizationMapper.mapDomainToModel(slide.getOrganization()));
        if (slideModel.getOrganizationOrder() == null) {
            SlideModel slideModelMaxOrder = slideRepository.findFirstByOrganization_IdOrderByOrganizationOrderDesc(slideModel.getOrganization().getId());
            slideModel.setOrganizationOrder(slideModelMaxOrder.getOrganizationOrder() + 1);
        } else {
            slideModel.setOrganizationOrder(slide.getOrganizationOrder());
        }
        slideModel.setImage(uploadImage(decodeImage(slide.getImage())));
        SlideModel save = slideRepository.save(slideModel);
        return SlideMapper.mapModelToDomain(save);
    }

    private Base64DecodedMultiPartFile decodeImage(String image) {
        byte[] decodeBytes = Base64.getDecoder().decode(image);
        return new Base64DecodedMultiPartFile(decodeBytes);
    }

    private String uploadImage(MultipartFile file) {
        return amazonService.uploadFile(file);
    }
}
