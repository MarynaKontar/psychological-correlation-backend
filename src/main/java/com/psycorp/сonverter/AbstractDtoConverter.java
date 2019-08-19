package com.psycorp.сonverter;

import com.psycorp.model.dto.AbstractDto;

import com.psycorp.model.entity.AbstractEntity;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract converter for all dtos (Data Transfer Object).
 * @param <Entity>
 * @param <Dto>
 */
public abstract class AbstractDtoConverter<Entity extends AbstractEntity, Dto extends AbstractDto> {

    public Dto transform(Entity entity) {
        Dto dto = createNewDto();
        doEntityToDtoTransform(entity, dto);
        return dto;
    }

    public Entity transform(Dto dto) {
        Entity entity = createEntity(dto);
        doDtoToEntityTransform(dto, entity);
        return entity;
    }

    public List<Dto> transform(Collection<Entity> entities) {
        if (CollectionUtils.isEmpty(entities)) return Collections.emptyList();
        return entities.stream().map(this::transform).collect(Collectors.toList());
    }

    public List<Entity> transform(List<Dto> entities) {
        if (CollectionUtils.isEmpty(entities)) return Collections.emptyList();
        return entities.stream().map(this::transform).collect(Collectors.toList());
    }

    private void doDtoToEntityTransform(Dto dto, Entity entity) {
        if (dto == null) return;
        updateEntitySystemFields(dto, entity);
        convertFromDto(dto, entity);
    }

    private void updateEntitySystemFields(Dto dto, Entity entity) {
        if (Objects.nonNull(dto) && Objects.nonNull(entity)) {
//            entity.setId(dto.getId());
        }
    }

    private Entity createEntity(Dto dto) {
        Entity entity = createNewEntity();
        return entity;
    }

    private void doEntityToDtoTransform(Entity entity, Dto dto) {
        if (entity == null) return;
        updateDtoSystemFields(entity, dto);
        convertFromEntity(entity, dto);
    }

    private void updateDtoSystemFields(Entity entity, Dto dto) {
        if (Objects.nonNull(entity) && Objects.nonNull(dto)) {
//            dto.setId(entity.getId());
        }
    }

    protected abstract Dto createNewDto();

    protected abstract Entity createNewEntity();

    protected abstract void convertFromEntity(Entity entity, Dto dto);

    protected abstract void convertFromDto(Dto dto, Entity entity);

}