package com.ngc.seaside.systemdescriptor.model.impl.view;

import com.google.common.base.Preconditions;
import com.ngc.seaside.systemdescriptor.model.api.INamedChildCollection;
import com.ngc.seaside.systemdescriptor.model.api.IPackage;
import com.ngc.seaside.systemdescriptor.model.api.data.IData;
import com.ngc.seaside.systemdescriptor.model.api.data.IDataField;
import com.ngc.seaside.systemdescriptor.model.api.metadata.IMetadata;
import com.ngc.seaside.systemdescriptor.model.impl.basic.NamedChildCollection;

import java.util.Optional;

public class HierarchicalDataView implements IData {

   private final IData wrapped;
   private final INamedChildCollection<IData, IDataField> aggregatedFields;

   public HierarchicalDataView(IData wrapped) {
      this.wrapped = Preconditions.checkNotNull(wrapped, "wrapped may not be null!");
      this.aggregatedFields = getAggregatedFields();
   }

   @Override
   public IMetadata getMetadata() {
      return wrapped.getMetadata();
   }

   @Override
   public IData setMetadata(IMetadata iMetadata) {
      return wrapped.setMetadata(iMetadata);
   }

   @Override
   public Optional<IData> getSuperDataType() {
      return wrapped.getSuperDataType();
   }

   @Override
   public IData setSuperDataType(IData iData) {
      return wrapped.setSuperDataType(iData);
   }

   @Override
   public INamedChildCollection<IData, IDataField> getFields() {
      return aggregatedFields;
   }

   @Override
   public String getFullyQualifiedName() {
      return wrapped.getFullyQualifiedName();
   }

   @Override
   public String getName() {
      return wrapped.getName();
   }

   @Override
   public IPackage getParent() {
      return wrapped.getParent();
   }

   @Override
   public String toString() {
      return wrapped.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o instanceof HierarchicalDataView) {
         return wrapped.equals(((HierarchicalDataView) o).wrapped);
      }
      return o instanceof IData && wrapped.equals(o);
   }

   @Override
   public int hashCode() {
      return wrapped.hashCode();
   }

   private INamedChildCollection<IData, IDataField> getAggregatedFields() {
      NamedChildCollection<IData, IDataField> collection = new NamedChildCollection<>();
      IData data = wrapped;
      while (data != null) {
         collection.addAll(data.getFields());
         data = data.getSuperDataType().orElse(null);
      }
      return collection;
   }
}
