/*
 * Copyright (C) 2013 RoboVM AB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
package org.robovm.compiler.llvm;

import org.robovm.llvm.Context;
import org.robovm.llvm.Module;
import org.robovm.llvm.Target;
import org.robovm.llvm.TargetMachine;

/**
 *
 * @version $Id$
 */
public class DataLayout {
    
    private final String triple;
    private final Target target;
    
    public DataLayout(String triple) {
        this.triple = triple;
        this.target = Target.lookupTarget(triple);
    }
    
    private <T> T runTypeQuery(Type type, TypeCallback<T> cb) {
        Context context = null;
        Module module = null;
        TargetMachine targetMachine = null;
        try {
            String definition;
            if (type instanceof PrimitiveType) {
                definition = "{" + ((PrimitiveType) type).getName() + "}";
            } else if (type instanceof VectorStructureType) {
                // if it is vector of primitive (E.g. VectorFloat2) it will return
                // <2 x float> like definition  and it will not go to
                // LLVM types that will cause a crash later, so wrap it into struct
                // And if it is vector of another structs (E.g. MatrixFloat2x2) it
                // will be wrapped into struct and nothing to be done
                VectorStructureType st = (VectorStructureType) type;
                if (!st.isVectorArray())
                    definition = "{" + ((StructureType) type).getDefinition() + "}";
                else
                    definition = ((StructureType) type).getDefinition();
            } else if (type instanceof StructureType) {
                definition = ((StructureType) type).getDefinition();
            } else {
                definition = "{" + ((UserType) type).getDefinition() + "}";
            }
            context = new Context();
            module = Module.parseIR(context, "%t = type " + definition, null);
            targetMachine = target.createTargetMachine(triple);
            return cb.doWithType(targetMachine, module.getTypeByName("t"));
        } finally {
            if (targetMachine != null) {
                targetMachine.dispose();
            }
            if (module != null) {
                module.dispose();
            }
            if (context != null) {
                context.dispose();
            }
        }
    }
    
    public int getAllocSize(Type type) {
        return runTypeQuery(type, new TypeCallback<Integer>() {
            Integer doWithType(TargetMachine targetMachine, org.robovm.llvm.Type type) {
                return (int) targetMachine.getDataLayout().getTypeAllocSize(type);
            }
        });
    }

    public int getAlignment(Type type) {
        return runTypeQuery(type, new TypeCallback<Integer>() {
            Integer doWithType(TargetMachine targetMachine, org.robovm.llvm.Type type) {
                return targetMachine.getDataLayout().getABITypeAlignment(type);
            }
        });
    }
    
    public int getStoreSize(Type type) {
        return runTypeQuery(type, new TypeCallback<Integer>() {
            Integer doWithType(TargetMachine targetMachine, org.robovm.llvm.Type type) {
                return (int) targetMachine.getDataLayout().getTypeStoreSize(type);
            }
        });
    }

    public int getOffsetOfElement(StructureType type, int idx) {
        if (type instanceof VectorStructureType) {
            // special case handling for simple vector arrays like VectorFloat2 that has llvm signature as <2 x float>
            // wrapping it into {<2 x float>} will fail on getting offset at index 1+ as there is only one element in
            // the list
            if (type.getOwnMembersOffset() != 0)
                throw new IllegalArgumentException("Failed to process Vectorized struct. Probably it inherits another struct!");

            // in this case just get storage size and multiply
            return getStoreSize(type.getTypeAt(0)) * idx;
        } else {
            return runTypeQuery(type, new TypeCallback<Integer>() {
                Integer doWithType(TargetMachine targetMachine, org.robovm.llvm.Type type) {
                    return (int) targetMachine.getDataLayout().getOffsetOfElement(type, idx);
                }
            });
        }
    }

    private static abstract class TypeCallback<T> {
        abstract T doWithType(TargetMachine targetMachine, org.robovm.llvm.Type type);
    }
}
