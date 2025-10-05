// ОЧЕНЬ ВАЖНО: убедитесь, что импортируете MaterialCommunityIcons
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { IconProps } from "react-native-paper/lib/typescript/components/MaterialCommunityIcon";

const MaterialCommunityIcon = ({ name, color, size, ...rest }: IconProps) => {
  return (
    <MaterialCommunityIcons
      name={name as any}
      color={color}
      size={size}
      {...rest}
    />
  );
};

export default MaterialCommunityIcon;
