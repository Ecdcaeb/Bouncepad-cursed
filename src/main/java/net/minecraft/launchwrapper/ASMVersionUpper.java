package net.minecraft.launchwrapper;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ASMVersionUpper implements IClassTransformer {
    private static final int mask = 65535; //0b1111111111111111

    private static final String[] classList = new String[]{
            "org/objectweb/asm/ClassVisitor",
            "org/objectweb/asm/MethodVisitor",
            "org/objectweb/asm/FieldVisitor"
    };

    private static final String[] interfaceList = new String[]{
            "net/minecraft/launchwrapper/IClassTransformer"
    };

    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0) {
            return bytes;
        }
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);


        String className = classReader.getClassName();
        boolean shouldTransform = shouldTransform(classReader);
        int version = 5;

        if (shouldTransform)
        {
            boolean modified = false;
            if (classNode.methods != null)
            {
                for (MethodNode methodNode : classNode.methods)
                {
                    InsnList instructions = methodNode.instructions;
                    if (instructions != null)
                    {
                        for (AbstractInsnNode insnNode : instructions)
                        {
                            if (insnNode.getOpcode() == Opcodes.LDC && insnNode instanceof LdcInsnNode ldcInsnNode)
                            {
                                if (ldcInsnNode.cst instanceof Integer value)
                                {
                                    if (value > 0) {
                                        version = value >> 16;
                                        if (version < 9 && (value & mask) == 0) {
                                            instructions.insert(ldcInsnNode, new LdcInsnNode(Opcodes.ASM9));
                                            instructions.remove(ldcInsnNode);
                                            modified = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (modified)
            {
                ClassWriter classWriter = new ClassWriter(0);
                LogWrapper.log(Level.WARN, "[Bouncepad] Uppatched ASM" + version + " to ASM9 on class: " + className + ", please port the mod to Cleanroom!");
                classNode.accept(classWriter);
                return classWriter.toByteArray();
            }
        }
        return bytes;
    }

    private static boolean shouldTransform(ClassReader classReader) {
        String superClassName = classReader.getSuperName();
        String[] interfaceNames = classReader.getInterfaces();
        for (String clazz : classList) {
            if (superClassName.equals(clazz)) {
                return true;
            }
        }

        for (String itf : interfaceList) {
            for (String name : interfaceNames) {
                if (name.equals(itf)) {
                    return true;
                }
            }
        }
        return false;
    }

}